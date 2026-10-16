package com.capstone.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 주기 작업(@Scheduled) 활성화. 현재는 기술 스택 키워드 맵 주기 재적재 (TS-20) */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
