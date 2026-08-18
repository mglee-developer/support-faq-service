package com.example.support_faq_service.domain.service;

import com.example.support_faq_service.domain.dto.FaqCreateRequest;
import com.example.support_faq_service.domain.dto.FaqResponse;
import com.example.support_faq_service.domain.entity.Faq;
import com.example.support_faq_service.domain.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {
    private final FaqRepository faqRepository;

    // 신규 FAQ 등록
    @Transactional
    public FaqResponse createFaq(FaqCreateRequest request) {
        Faq faq = Faq.builder()
                .category(request.getCategory())
                .answer(request.getAnswer())
                .question(request.getQuestion())
                .viewCnt(0)
                .build();

        Faq savedFaq = faqRepository.save(faq);

        return new FaqResponse(savedFaq);
    }

    // 카테고리별 FAQ 목록 조회
    public List<FaqResponse> getFaqs(String category) {

        return faqRepository.findByCategory(category)
                .stream()
                .map(FaqResponse::new)
                .toList();
    }

    // 조회수 상위 10개 인기 FAQ 조회
    public List<FaqResponse> getTopPopularFaqs() {
        return faqRepository.findTop10ByOrderByViewCntDesc()
                .stream()
                .map(FaqResponse::new)
                .toList();
    }

    // 키워드 검색 및 조회수 증가
    public List<FaqResponse> searchFaqsByKeyword(String keyword) {
        return faqRepository.findByQuestionContaining(keyword)
                .stream()
                .map(FaqResponse::new)
                .toList();
    }
}
