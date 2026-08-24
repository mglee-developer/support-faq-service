package com.example.support_faq_service.domain.controller;

import com.example.support_faq_service.domain.dto.SearchHistoryResponse;
import com.example.support_faq_service.domain.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search-histories")
@RequiredArgsConstructor
public class SearchHistoryController {
    private final SearchHistoryService searchHistoryService;

    // 최근 검색/대화 이력 조회
    @GetMapping
    public ResponseEntity<List<SearchHistoryResponse>> getRecentSearchHistories() {
        List<SearchHistoryResponse> response = searchHistoryService.getRecentSearchHistories();
        return ResponseEntity.ok(response);
    }
}
