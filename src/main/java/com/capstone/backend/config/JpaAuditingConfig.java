package com.capstone.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @CreatedDate, @LastModifiedDate 자동 기록.
 * 메인 클래스가 아닌 별도 설정 클래스에 두어 @WebMvcTest 같은 슬라이스 테스트에서는 로딩되지 않게 한다 (TS-07)
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
