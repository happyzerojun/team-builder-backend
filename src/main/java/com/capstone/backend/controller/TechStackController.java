package com.capstone.backend.controller;

import com.capstone.backend.dto.TechStackResponseDto;
import com.capstone.backend.service.TechStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
