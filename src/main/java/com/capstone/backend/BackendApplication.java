package com.capstone.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
    
@EnableJpaAuditing // @CreatedDate와 @LastModifiedDate가 작동해서 시간이 자동으로 찍히게 하기 위함
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
