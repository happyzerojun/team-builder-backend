package com.capstone.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// JPA Auditing(@CreatedDate, @LastModifiedDate)은 config/JpaAuditingConfig 로 옮겼다.
// 메인 클래스에 두면 @WebMvcTest 슬라이스 테스트까지 JPA 설정을 요구해 컨텍스트 로딩이 실패한다 (TS-07)
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
