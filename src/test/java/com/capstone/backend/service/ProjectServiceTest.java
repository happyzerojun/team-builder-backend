package com.capstone.backend.service;

import com.capstone.backend.dto.ProjectRequestDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.global.exception.ForbiddenException;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ProjectPositionRepository;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.ProjectTechStackRepository;
import com.capstone.backend.repository.ReviewRepository;
import com.capstone.backend.repository.TechStackRepository;
import com.capstone.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** TS-05: 모집글 변경은 작성자만, 작성자는 토큰의 사용자 */
@ExtendWith(MockitoExtension.class)
@DisplayName("모집글 소유권")
class ProjectServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private TechStackRepository techStackRepository;
    @Mock private ProjectTechStackRepository projectTechStackRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ProjectPositionRepository projectPositionRepository;
    @Mock private PositionSeats positionSeats;

    @InjectMocks
    private ProjectService projectService;

    private final User leader = User.builder().id(1L).email("leader@example.com").name("팀장").build();
    private final User stranger = User.builder().id(3L).email("other@example.com").name("남").build();

    private Project project() {
        return Project.builder().id(10L).title("원래 제목").content("c").region("온라인").term("3")
                .status("모집중").leader(leader).build();
    }

    private static ProjectRequestDto request(String title) {
        ProjectRequestDto dto = new ProjectRequestDto();
        ReflectionTestUtils.setField(dto, "title", title);
        ReflectionTestUtils.setField(dto, "content", "내용");
        ReflectionTestUtils.setField(dto, "region", "온라인");
        ReflectionTestUtils.setField(dto, "term", 3);
        return dto;
    }

    @Test
    @DisplayName("생성 시 작성자는 토큰의 사용자이고, 같은 트랜잭션에서 팀장으로 팀에 등록된다")
    void createRegistersLeaderAsMember() {
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(leader));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 10L);
            return p;
        });

        projectService.createProject("leader@example.com", request("새 글"));

        ArgumentCaptor<Application> membership = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(membership.capture());
        assertThat(membership.getValue().getApplicant()).isSameAs(leader);
        assertThat(membership.getValue().getStatus()).isEqualTo("ACCEPTED");
        assertThat(membership.getValue().getSupportRole()).isEqualTo("팀장");
    }

    @Test
    @DisplayName("작성자가 아니면 수정·삭제·상태변경·팀원관리 모두 403, 데이터는 그대로")
    void nonLeaderIsForbidden() {
        Project project = project();
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(stranger));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateProject("other@example.com", 10L, request("바꾼 제목")))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> projectService.deleteProject("other@example.com", 10L))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> projectService.updateProjectStatus("other@example.com", 10L, "진행중"))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> projectService.removeProjectMember("other@example.com", 10L, 2L))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> projectService.addProjectMember("other@example.com", 10L, 3L))
                .isInstanceOf(ForbiddenException.class);

        assertThat(project.getTitle()).isEqualTo("원래 제목");
        assertThat(project.getStatus()).isEqualTo("모집중");
        verify(projectRepository, never()).delete(any());
        verify(reviewRepository, never()).deleteAllByProjectId(anyLong());
    }

    @Test
    @DisplayName("작성자는 삭제할 수 있고, 리뷰(FK)를 먼저 지운다")
    void leaderDeletesWithReviews() {
        Project project = project();
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(leader));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        projectService.deleteProject("leader@example.com", 10L);

        verify(reviewRepository).deleteAllByProjectId(10L);
        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("팀장 자신은 팀에서 제외할 수 없다")
    void cannotRemoveLeader() {
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(leader));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project()));

        assertThatThrownBy(() -> projectService.removeProjectMember("leader@example.com", 10L, 1L))
                .isInstanceOf(ConflictException.class);
    }
}
