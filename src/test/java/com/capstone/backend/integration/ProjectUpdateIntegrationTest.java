package com.capstone.backend.integration;

import com.capstone.backend.dto.PositionRequestDto;
import com.capstone.backend.dto.ProjectRequestDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.ProjectPosition;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ProjectPositionRepository;
import com.capstone.backend.repository.TechStackRepository;
import com.capstone.backend.service.ApplicationService;
import com.capstone.backend.service.ProjectService;
import com.capstone.backend.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 모집글 수정 API가 기술 스택(TS-18)과 포지션(TS-19)을 실제로 반영하는지 (MySQL) */
@DisplayName("모집글 수정 (MySQL)")
class ProjectUpdateIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TechStackRepository techStackRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectPositionRepository projectPositionRepository;

    /** 지원 후 팀장이 수락까지 (정원 +1) */
    private Application acceptedMember(User leader, long projectId, String role) {
        Application application = pendingApplication(projectId, newUser("upd-member"), role);
        applicationService.updateStatus(leader.getEmail(), application.getId(), "ACCEPTED");
        return application;
    }

    private Application pendingApplication(long projectId, User applicant, String role) {
        return applicationRepository.save(Application.builder()
                .project(projectRepository.findById(projectId).orElseThrow())
                .applicant(applicant).supportRole(role).status("PENDING").build());
    }

    /** 포지션 상태를 "이름=현재/정원" 문자열로 (순서 무관 비교용) */
    private List<String> positions(long projectId) {
        return projectPositionRepository.findByProjectId(projectId).stream()
                .map(p -> p.getRoleName() + "=" + p.getCurrentCount() + "/" + p.getRequiredCount())
                .toList();
    }

    private static ProjectRequestDto requestWithPositions(Object... nameAndCount) {
        ProjectRequestDto dto = new ProjectRequestDto();
        ReflectionTestUtils.setField(dto, "title", "동시성");
        ReflectionTestUtils.setField(dto, "content", "내용");
        ReflectionTestUtils.setField(dto, "region", "온라인");
        ReflectionTestUtils.setField(dto, "term", 3);
        List<PositionRequestDto> positions = new ArrayList<>();
        for (int i = 0; i < nameAndCount.length; i += 2) {
            PositionRequestDto position = new PositionRequestDto();
            ReflectionTestUtils.setField(position, "roleName", nameAndCount[i]);
            ReflectionTestUtils.setField(position, "requiredCount", nameAndCount[i + 1]);
            positions.add(position);
        }
        ReflectionTestUtils.setField(dto, "positions", positions);
        return dto;
    }

    private long stackId(String name) {
        return techStackRepository.findByName(name).orElseThrow().getId();   // DataInitializer 초기 데이터
    }

    private long createProject(User leader, String extraJson) throws Exception {
        String created = mockMvc.perform(post("/api/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leader))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "수정 테스트", "content": "내용", "region": "온라인", "term": 3 %s}
                                """.formatted(extraJson)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long projectId = ((Number) JsonPath.read(created, "$.project_id")).longValue();
        trackProject(projectId);
        return projectId;
    }

    private ResultActions update(User leader, long projectId, String extraJson) throws Exception {
        return mockMvc.perform(put("/api/projects/" + projectId)
                .header(HttpHeaders.AUTHORIZATION, bearer(leader))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "수정한 제목", "content": "내용", "region": "온라인", "term": 3 %s}
                        """.formatted(extraJson)));
    }

    private List<String> stackNames(long projectId) throws Exception {
        String body = mockMvc.perform(get("/api/projects/" + projectId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.techStacks[*].name");
    }

    @Test
    @DisplayName("기술 스택: 생성 응답에 포함되고, 수정하면 교체·빈 목록이면 해제·필드가 없으면 유지")
    void techStacksAreReplacedOnUpdate() throws Exception {
        User leader = newUser("upd-stack");
        long react = stackId("React");
        long java = stackId("Java");
        long docker = stackId("Docker");

        long projectId = createProject(leader, ", \"techStackIds\": [%d, %d]".formatted(react, java));
        assertThat(stackNames(projectId)).containsExactlyInAnyOrder("React", "Java");

        // 교체: React 유지, Java 제거, Docker 추가 (중복 ID는 한 번만)
        update(leader, projectId, ", \"techStackIds\": [%d, %d, %d]".formatted(react, docker, docker))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정한 제목"))
                .andExpect(jsonPath("$.techStacks.length()").value(2));
        assertThat(stackNames(projectId)).containsExactlyInAnyOrder("React", "Docker");

        // techStackIds 필드 없음 → 기존 유지
        update(leader, projectId, "").andExpect(status().isOk());
        assertThat(stackNames(projectId)).containsExactlyInAnyOrder("React", "Docker");

        // 빈 목록 → 모두 해제
        update(leader, projectId, ", \"techStackIds\": []").andExpect(status().isOk());
        assertThat(stackNames(projectId)).isEmpty();
    }

    @Test
    @DisplayName("생성 응답에도 저장한 기술 스택이 들어 있다")
    void createResponseIncludesTechStacks() throws Exception {
        User leader = newUser("upd-create");
        mockMvc.perform(post("/api/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leader))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "생성 응답", "content": "내용", "region": "온라인", "term": 3, "techStackIds": [%d]}
                                """.formatted(stackId("Spring Boot"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.techStacks[0].name").value("Spring Boot"))
                .andDo(result -> trackProject(((Number) JsonPath.read(
                        result.getResponse().getContentAsString(), "$.project_id")).longValue()));
    }

    @Test
    @DisplayName("포지션: 인원 변경·삭제·추가를 한 번에 반영하고, 필드가 없으면 유지")
    void positionsAreSyncedOnUpdate() throws Exception {
        User leader = newUser("upd-pos");
        long projectId = createProject(leader, """
                , "positions": [{"roleName": "백엔드", "requiredCount": 2}, {"roleName": "프론트엔드", "requiredCount": 1}]""");
        acceptedMember(leader, projectId, "백엔드");

        // 백엔드 2 → 1(합류 1명이라 가능), 프론트엔드 삭제(합류 0명), 디자이너 추가
        update(leader, projectId, """
                , "positions": [{"roleName": "백엔드", "requiredCount": 1}, {"roleName": " 디자이너 ", "requiredCount": 2}]""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.positions.length()").value(2));
        assertThat(positions(projectId)).containsExactlyInAnyOrder("백엔드=1/1", "디자이너=0/2");

        // positions 필드 없음 → 유지
        update(leader, projectId, "").andExpect(status().isOk());
        assertThat(positions(projectId)).containsExactlyInAnyOrder("백엔드=1/1", "디자이너=0/2");
    }

    @Test
    @DisplayName("포지션: 합류 인원보다 적게 줄이거나 합류한 팀원이 있는 포지션을 지우면 409, 변경 전체 롤백")
    void positionChangesBelowMembersAreRejected() throws Exception {
        User leader = newUser("upd-pos2");
        long projectId = createProject(leader, """
                , "positions": [{"roleName": "백엔드", "requiredCount": 3}, {"roleName": "기획", "requiredCount": 1}]""");
        acceptedMember(leader, projectId, "백엔드");
        acceptedMember(leader, projectId, "백엔드");

        // 디자이너 추가 + 백엔드 3 → 1(합류 2명): 409, 디자이너 추가도 롤백
        update(leader, projectId, """
                , "positions": [{"roleName": "백엔드", "requiredCount": 1}, {"roleName": "기획", "requiredCount": 1},
                                {"roleName": "디자이너", "requiredCount": 1}]""")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
        // 백엔드 삭제(합류 2명): 409
        update(leader, projectId, """
                , "positions": [{"roleName": "기획", "requiredCount": 1}]""")
                .andExpect(status().isConflict());
        assertThat(positions(projectId)).containsExactlyInAnyOrder("백엔드=2/3", "기획=0/1");

        // 합류 인원과 같게 줄이는 것은 가능
        update(leader, projectId, """
                , "positions": [{"roleName": "백엔드", "requiredCount": 2}, {"roleName": "기획", "requiredCount": 1}]""")
                .andExpect(status().isOk());
        assertThat(positions(projectId)).containsExactlyInAnyOrder("백엔드=2/2", "기획=0/1");
    }

    @Test
    @DisplayName("포지션: 같은 이름 두 번·예약어(팀장)·0명·11개 이상은 400")
    void invalidPositionsAre400() throws Exception {
        User leader = newUser("upd-pos3");
        long projectId = createProject(leader, "");

        update(leader, projectId, """
                , "positions": [{"roleName": "백엔드", "requiredCount": 1}, {"roleName": " 백엔드", "requiredCount": 2}]""")
                .andExpect(status().isBadRequest());
        update(leader, projectId, """
                , "positions": [{"roleName": "팀장", "requiredCount": 1}]""")
                .andExpect(status().isBadRequest());
        update(leader, projectId, """
                , "positions": [{"roleName": "백엔드", "requiredCount": 0}]""")
                .andExpect(status().isBadRequest());
        String eleven = IntStream.range(0, 11)
                .mapToObj(i -> "{\"roleName\": \"역할" + i + "\", \"requiredCount\": 1}")
                .collect(Collectors.joining(", "));
        update(leader, projectId, ", \"positions\": [" + eleven + "]")
                .andExpect(status().isBadRequest());
        assertThat(positions(projectId)).isEmpty();
    }

    @Test
    @DisplayName("포지션: 삭제된 포지션으로 들어온 대기 지원서는 수락하면 409 (정원 검사 우회 방지)")
    void pendingApplicationForRemovedPositionCannotBeAccepted() throws Exception {
        User leader = newUser("upd-pos4");
        long projectId = createProject(leader, """
                , "positions": [{"roleName": "백엔드", "requiredCount": 1}, {"roleName": "프론트엔드", "requiredCount": 1}]""");
        Application pending = pendingApplication(projectId, newUser("upd-pending"), "프론트엔드");

        update(leader, projectId, """
                , "positions": [{"roleName": "백엔드", "requiredCount": 1}]""")
                .andExpect(status().isOk());

        assertThatThrownBy(() -> applicationService.updateStatus(leader.getEmail(), pending.getId(), "ACCEPTED"))
                .isInstanceOf(ConflictException.class);
        assertThat(applicationRepository.findById(pending.getId()).orElseThrow().isAccepted()).isFalse();
    }

    @Test
    @DisplayName("포지션: 인원 축소와 지원 수락이 동시에 와도 합류 인원이 정원을 넘지 않는다")
    void concurrentShrinkAndAcceptNeverExceedCapacity() throws Exception {
        User leader = newUser("upd-pos5");
        long projectId = createProject(leader, """
                , "positions": [{"roleName": "백엔드", "requiredCount": 2}]""");
        acceptedMember(leader, projectId, "백엔드");                                   // 1/2
        Application pending = pendingApplication(projectId, newUser("upd-race"), "백엔드");

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Future<String> shrink = pool.submit(() -> {
            start.await();
            try {
                projectService.updateProject(leader.getEmail(), projectId, requestWithPositions("백엔드", 1));
                return "SHRUNK";
            } catch (ConflictException e) {
                return "SHRINK_REJECTED";
            }
        });
        Future<String> accept = pool.submit(() -> {
            start.await();
            try {
                applicationService.updateStatus(leader.getEmail(), pending.getId(), "ACCEPTED");
                return "ACCEPTED";
            } catch (ConflictException e) {
                return "ACCEPT_REJECTED";
            }
        });
        start.countDown();
        List<String> results = List.of(shrink.get(30, TimeUnit.SECONDS), accept.get(30, TimeUnit.SECONDS));
        pool.shutdown();

        // 축소가 먼저면 1/1이 돼 수락 실패, 수락이 먼저면 2/2가 돼 축소 실패. 둘 다 성공하는 경우는 없다
        assertThat(results).containsAnyOf("SHRUNK", "ACCEPTED");
        assertThat(results).containsAnyOf("SHRINK_REJECTED", "ACCEPT_REJECTED");
        ProjectPosition position = projectPositionRepository.findByProjectId(projectId).get(0);
        assertThat(position.getCurrentCount()).isLessThanOrEqualTo(position.getRequiredCount());
    }
}
