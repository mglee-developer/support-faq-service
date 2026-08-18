package com.example.support_faq_service.domain.dto;

import com.example.support_faq_service.domain.entity.SearchHistory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SearchHistoryResponse {
    private Long id;
    private String userQuery;
    private String aiResponse;
    private LocalDateTime createdAt;

    public SearchHistoryResponse(SearchHistory searchHistory) {
        this.id = searchHistory.getId();
        this.userQuery = searchHistory.getUserQuery();
        this.aiResponse = searchHistory.getAiResponse();
        this.createdAt = searchHistory.getCreatedAt();
    }
}
