package com.capstone.backend.service;

import com.capstone.backend.dto.TechStackResponseDto;
import com.capstone.backend.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechStackService {

    private final TechStackRepository techStackRepository;

    public List<TechStackResponseDto> getAllTechStacks() {
        return techStackRepository.findAll().stream()
                .map(TechStackResponseDto::new)
                .collect(Collectors.toList());
    }
}