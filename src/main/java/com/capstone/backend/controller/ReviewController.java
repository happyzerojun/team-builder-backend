package com.capstone.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    // 6. 팀원 다중 리뷰 일괄 등록 (POST /api/review/bulk)
    @PostMapping("/bulk")
    public ResponseEntity<String> createReviewsBulk() {
        // TODO: RequestBody로 리뷰 리스트를 받아 한 번에 DB에 저장하는 로직 추가
        return ResponseEntity.ok("팀원 다중 리뷰 일괄 등록 완료 (API 연결 테스트)");
    }
}