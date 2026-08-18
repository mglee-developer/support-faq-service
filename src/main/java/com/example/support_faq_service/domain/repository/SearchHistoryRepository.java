package com.example.support_faq_service.domain.repository;

import com.example.support_faq_service.domain.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    // 최근 검색 이력 조회
    List<SearchHistory> findByOrderByCreatedAtDesc();
}
