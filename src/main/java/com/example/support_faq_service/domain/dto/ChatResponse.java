package com.example.support_faq_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private String sessionId;
    private String aiResponse;  // AI가 생성한 최종 답변 문장
    private List<FaqResponse> referencedFaqs;   // AI 답변 생성 시 참고한 FAQ 목록
    private boolean isFallback;                 // AI 장애 또는 FAQ 검색 실패로 1:1 문의 접수 안내가 나갔는지 여부
}
