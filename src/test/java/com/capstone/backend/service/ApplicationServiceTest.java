package com.capstone.backend.service;

import com.capstone.backend.dto.ApplicationRequestDto;
import com.capstone.backend.dto.ApplicationResponseDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.global.exception.ForbiddenException;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** TS-05: 지원서 소유권 검사 (요청자 = 토큰의 사용자) */
@ExtendWith(MockitoExtension.class)
@DisplayName("지원서 소유권")
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private PositionSeats positionSeats;

    @InjectMocks
    private ApplicationService applicationService;

    private final User leader = User.builder().id(1L).email("leader@example.com").name("팀장").build();
    private final User applicant = User.builder().id(2L).email("me@example.com").name("지원자").build();
    private final User stranger = User.builder().id(3L).email("other@example.com").name("남").build();
    private final Project project = Project.builder().id(10L).title("글").status("모집중").leader(leader).build();

    private Application application(User who, String status) {
        return Application.builder().id(100L).project(project).applicant(who).status(status).build();
    }

    private static ApplicationRequestDto.Create createDto(Long projectId) {
        ApplicationRequestDto.Create dto = new ApplicationRequestDto.Create();
        ReflectionTestUtils.setField(dto, "project_id", projectId);
        ReflectionTestUtils.setField(dto, "support_role", "백엔드");
        return dto;
    }

    @Test
    @DisplayName("지원자는 토큰의 사용자로 저장된다")
    void createUsesCurrentUser() {
        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(applicant));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        applicationService.createApplication("me@example.com", createDto(10L));

        ArgumentCaptor<Application> saved = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(saved.capture());
        assertThat(saved.getValue().getApplicant()).isSameAs(applicant);
        assertThat(saved.getValue().getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("자기 모집글 지원·마감된 글 지원은 409")
    void cannotApplyToOwnOrClosedProject() {
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(leader));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> applicationService.createApplication("leader@example.com", createDto(10L)))
                .isInstanceOf(ConflictException.class);

        Project closed = Project.builder().id(11L).status("진행중").leader(leader).build();
        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(applicant));
        when(projectRepository.findById(11L)).thenReturn(Optional.of(closed));
        assertThatThrownBy(() -> applicationService.createApplication("me@example.com", createDto(11L)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("토큰의 사용자가 DB에 없으면 401")
    void unknownUserIs401() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> applicationService.getMyApplications("ghost@example.com"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("팀장이 아닌 사용자의 수락·거절은 403이고 상태가 바뀌지 않는다")
    void onlyLeaderCanChangeStatus() {
        Application pending = application(applicant, "PENDING");
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(stranger));
        when(applicationRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> applicationService.updateStatus("other@example.com", 100L, "ACCEPTED"))
                .isInstanceOf(ForbiddenException.class);
        assertThat(pending.getStatus()).isEqualTo("PENDING");
        verify(positionSeats, never()).take(any(), any());
    }

    @Test
    @DisplayName("TS-06: 수락하면 포지션 정원을 차지하고, 같은 요청 반복(멱등)은 다시 세지 않는다")
    void acceptTakesSeatOnce() {
        Application pending = Application.builder().id(100L).project(project).applicant(applicant)
                .supportRole("백엔드").status("PENDING").build();
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(leader));
        when(applicationRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(pending));

        applicationService.updateStatus("leader@example.com", 100L, "ACCEPTED");
        applicationService.updateStatus("leader@example.com", 100L, "ACCEPTED");

        verify(positionSeats, times(1)).take(10L, "백엔드");
        assertThat(pending.getStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    @DisplayName("TS-06: 정원 초과(조건부 UPDATE 0행)는 409로 전파된다")
    void fullPositionIs409() {
        Application pending = Application.builder().id(100L).project(project).applicant(applicant)
                .supportRole("백엔드").status("PENDING").build();
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(leader));
        when(applicationRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(pending));
        doThrow(new ConflictException("'백엔드' 포지션은 정원이 찼습니다.")).when(positionSeats).take(10L, "백엔드");

        assertThatThrownBy(() -> applicationService.updateStatus("leader@example.com", 100L, "ACCEPTED"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("TS-06: 모집이 끝난(진행중) 프로젝트에서는 수락할 수 없다")
    void cannotAcceptAfterRecruiting() {
        Project proceeding = Project.builder().id(12L).status("진행중").leader(leader).build();
        Application pending = Application.builder().id(100L).project(proceeding).applicant(applicant).status("PENDING").build();
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(leader));
        when(applicationRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> applicationService.updateStatus("leader@example.com", 100L, "ACCEPTED"))
                .isInstanceOf(ConflictException.class);
        verify(positionSeats, never()).take(any(), any());
    }

    @Test
    @DisplayName("TS-06: 수락된 지원자가 취소하면 정원을 돌려준다")
    void cancelReleasesSeat() {
        Application accepted = Application.builder().id(100L).project(project).applicant(applicant)
                .supportRole("백엔드").status("ACCEPTED").build();
        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(applicant));
        when(applicationRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(accepted));

        applicationService.deleteApplication("me@example.com", 100L);

        verify(positionSeats).releaseIfAccepted(accepted);
        verify(applicationRepository).delete(accepted);
    }

    @Test
    @DisplayName("지원 취소는 지원자 본인만")
    void onlyApplicantCanCancel() {
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(stranger));
        when(applicationRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(application(applicant, "PENDING")));

        assertThatThrownBy(() -> applicationService.deleteApplication("other@example.com", 100L))
                .isInstanceOf(ForbiddenException.class);
        verify(applicationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("다른 사람의 userId로 지원 내역 조회는 403")
    void cannotReadOthersApplications() {
        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(applicant));
        assertThatThrownBy(() -> applicationService.getApplicationsByUserId("me@example.com", 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("프로젝트 지원서 목록: 팀원은 수락된 지원서만, 외부인은 403")
    void projectApplicationsVisibility() {
        Application member = Application.builder().id(101L).project(project).applicant(applicant).status("ACCEPTED").build();
        Application waiting = Application.builder().id(102L).project(project).applicant(stranger).status("PENDING").build();
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(applicationRepository.findByProjectId(10L)).thenReturn(List.of(member, waiting));

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(applicant));
        List<ApplicationResponseDto> visibleToMember = applicationService.getApplicationsByProjectId("me@example.com", 10L);
        assertThat(visibleToMember).extracting(ApplicationResponseDto::getApplicationId).containsExactly(101L);

        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(leader));
        assertThat(applicationService.getApplicationsByProjectId("leader@example.com", 10L)).hasSize(2);

        User outsider = User.builder().id(4L).email("out@example.com").build();
        when(userRepository.findByEmail("out@example.com")).thenReturn(Optional.of(outsider));
        assertThatThrownBy(() -> applicationService.getApplicationsByProjectId("out@example.com", 10L))
                .isInstanceOf(ForbiddenException.class);
    }
}
