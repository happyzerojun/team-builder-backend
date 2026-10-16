package com.capstone.backend.integration;

import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.ProjectPosition;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ProjectPositionRepository;
import com.capstone.backend.service.ApplicationService;
import com.capstone.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** TS-06: 실제 MySQL에서 동시 수락이 정원을 넘지 않는지 */
@DisplayName("포지션 정원 동시성 (MySQL)")
class PositionCapacityIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ProjectPositionRepository projectPositionRepository;

    private Application pendingApplication(Project project, User applicant, String role) {
        return applicationRepository.save(Application.builder()
                .project(project).applicant(applicant).supportRole(role).status("PENDING").build());
    }

    @Test
    @DisplayName("정원 1명 포지션에 두 지원서를 동시에 수락하면 한 건만 성공하고 나머지는 409")
    void concurrentAcceptsDoNotExceedCapacity() throws Exception {
        User leader = newUser("cap-leader");
        Project project = newProject(leader, "정원 동시성 테스트");
        ProjectPosition position = projectPositionRepository.save(ProjectPosition.builder()
                .project(project).roleName("백엔드").requiredCount(1).currentCount(0).build());

        List<Long> applicationIds = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            applicationIds.add(pendingApplication(project, newUser("cap-applicant" + i), "백엔드").getId());
        }

        ExecutorService pool = Executors.newFixedThreadPool(applicationIds.size());
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();
        for (Long id : applicationIds) {
            Callable<String> accept = () -> {
                start.await();
                try {
                    applicationService.updateStatus(leader.getEmail(), id, "ACCEPTED");
                    return "OK";
                } catch (ConflictException e) {
                    return "FULL";
                }
            };
            futures.add(pool.submit(accept));
        }
        start.countDown();
        List<String> results = new ArrayList<>();
        for (Future<String> f : futures) {
            results.add(f.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();

        assertThat(results).containsOnlyOnce("OK");
        assertThat(results).filteredOn("FULL"::equals).hasSize(applicationIds.size() - 1);
        assertThat(projectPositionRepository.findById(position.getId()).orElseThrow().getCurrentCount()).isEqualTo(1);
        // 실패한 수락은 지원서 상태 변경까지 롤백돼 PENDING으로 남는다
        long accepted = applicationIds.stream()
                .map(id -> applicationRepository.findById(id).orElseThrow())
                .filter(Application::isAccepted)
                .count();
        assertThat(accepted).isEqualTo(1);
    }

    @Test
    @DisplayName("수락된 팀원이 지원을 취소하면 정원이 다시 비고, 다음 지원자를 수락할 수 있다")
    void cancelReleasesSeat() {
        User leader = newUser("cap2-leader");
        Project project = newProject(leader, "정원 반환 테스트");
        ProjectPosition position = projectPositionRepository.save(ProjectPosition.builder()
                .project(project).roleName("프론트엔드").requiredCount(1).currentCount(0).build());
        User first = newUser("cap2-first");
        User second = newUser("cap2-second");
        Application a = pendingApplication(project, first, "프론트엔드");
        Application b = pendingApplication(project, second, "프론트엔드");

        applicationService.updateStatus(leader.getEmail(), a.getId(), "ACCEPTED");
        applicationService.deleteApplication(first.getEmail(), a.getId());
        assertThat(projectPositionRepository.findById(position.getId()).orElseThrow().getCurrentCount()).isZero();

        applicationService.updateStatus(leader.getEmail(), b.getId(), "ACCEPTED");
        assertThat(projectPositionRepository.findById(position.getId()).orElseThrow().getCurrentCount()).isEqualTo(1);
    }
}
