package com.example.support_faq_service.domain.service;

import com.example.support_faq_service.domain.dto.SearchHistoryResponse;
import com.example.support_faq_service.domain.entity.SearchHistory;
import com.example.support_faq_service.domain.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchHistoryService {
    private final SearchHistoryRepository searchHistoryRepository;

    // 검색/질문 이력 저장
    @Transactional
    public void saveSearchHistory(String userQuery, String aiResponse) {
        SearchHistory searchHistory = SearchHistory
                                    .builder()
                                    .userQuery(userQuery)
                                    .aiResponse(aiResponse)
                                    .build();
        searchHistoryRepository.save(searchHistory);
    }

    // 최근 검색 이력 목록 조회
    public List<SearchHistoryResponse> getRecentSearchHistories() {
        return searchHistoryRepository.findByOrderByCreatedAtDesc()
                .stream()
                .map(SearchHistoryResponse::new)
                .toList();
    }
}
