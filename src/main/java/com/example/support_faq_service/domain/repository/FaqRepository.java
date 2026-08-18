package com.example.support_faq_service.domain.repository;

import com.example.support_faq_service.domain.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    // 카테고리별 FAQ 목록 조회
    List<Faq> findByCategory(String category);

    // 질문 키워드 검색
    List<Faq> findByQuestionContaining(String question);

    // 인기 FAQ 조회(조회수 기준 내림차순 상위 N개)
    List<Faq> findTop10ByOrderByViewCntDesc();

}
