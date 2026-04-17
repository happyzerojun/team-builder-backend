package com.capstone.backend.controller;

import com.capstone.backend.dto.TechStackResponseDto;
import com.capstone.backend.service.TechStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 🔥 영준님의 센스: 프론트엔드 수연님이 CORS 에러로 고통받지 않게 미리 뚫어줌!
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tech-stacks")
@RequiredArgsConstructor
public class TechStackController {

    private final TechStackService techStackService;

    @GetMapping
    public ResponseEntity<List<TechStackResponseDto>> getAllTechStacks() {
        List<TechStackResponseDto> response = techStackService.getAllTechStacks();
        return ResponseEntity.ok(response);
    }
}