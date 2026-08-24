package com.example.support_faq_service.domain.controller;

import com.example.support_faq_service.domain.dto.FaqCreateRequest;
import com.example.support_faq_service.domain.dto.FaqResponse;
import com.example.support_faq_service.domain.service.FaqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faqs")
@RequiredArgsConstructor
public class FaqController {
    private final FaqService faqService;

    // FAQ 신규 등록
    @PostMapping
    public ResponseEntity<FaqResponse> createFaq(@Valid @RequestBody FaqCreateRequest faqCreateRequest) {
        FaqResponse faqResponse = faqService.createFaq(faqCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(faqResponse);
    }

    // 카테고리별 FAQ 목록 조회
    @GetMapping
    public ResponseEntity<List<FaqResponse>> getFaqsByCategory(@RequestParam String category) {
        List<FaqResponse> response = faqService.getFaqs(category);
        return ResponseEntity.ok(response);
    }

    // 인기 FAQ 상위 10개 조회
    @GetMapping("/popular")
    public ResponseEntity<List<FaqResponse>> getTopPopularFaqs() {
        List<FaqResponse> response = faqService.getTopPopularFaqs();
        return ResponseEntity.ok(response);
    }

    // 키워드 FAQ 검색
    public ResponseEntity<List<FaqResponse>> searchFaqsByKeyword(@RequestParam String keyword) {
        List<FaqResponse> response = faqService.searchFaqsByKeyword(keyword);
        return ResponseEntity.ok(response);
    }
}
