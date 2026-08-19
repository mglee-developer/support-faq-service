package com.example.support_faq_service.domain.service;

import com.example.support_faq_service.domain.dto.ChatRequest;
import com.example.support_faq_service.domain.dto.ChatResponse;
import com.example.support_faq_service.domain.dto.FaqResponse;
import com.example.support_faq_service.domain.repository.FaqRepository;
import com.example.support_faq_service.domain.repository.SearchHistoryRepository;
import com.example.support_faq_service.infrastructure.client.LlmClient;
import io.micrometer.core.instrument.search.Search;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final FaqRepository faqRepository;
    private final LlmClient llmClient;
    private final SearchHistoryService searchHistoryService;

    @Transactional
    public ChatResponse processChat(ChatRequest request) {
        String userQuery = request.getUserQuery();
        String sessionId = request.getSessionId();

        // 1. RAG 키워드 검색
        List<FaqResponse> referencedFaqs = faqRepository.findByQuestionContaining(userQuery)
                .stream()
                .map(FaqResponse::new)
                .limit(3)
                .toList();

        // 2. FAQ 검색 결과가 없는 경우 -> Fallback
        if(referencedFaqs.isEmpty()) {
            return createFallbackResponse(sessionId, userQuery);
        }

        try {
            // 3. System Prompt 동적 조합
            String systemPrompt = buildSystemPrompt(referencedFaqs);

            // 4. LLM API (Mock) 호출
            String aiResponse = llmClient.generateAnswer(systemPrompt, userQuery);

            // 5. 질문 및 AI 답변 이력 저장
            searchHistoryService.saveSearchHistory(userQuery, aiResponse);

            return ChatResponse.builder()
                    .sessionId(sessionId)
                    .aiResponse(aiResponse)
                    .referencedFaqs(referencedFaqs)
                    .isFallback(false)
                    .build();
        } catch(Exception e) {
            // 6. LLM 호출 중 에러 발생 시 Fallback 처리
            return createFallbackResponse(sessionId, userQuery);
        }
    }

    // System Prompt 생성 메서드
    private String buildSystemPrompt(List<FaqResponse> faqs) {
        StringBuilder sb = new StringBuilder();
        sb.append("너는 고객지원의 친절한 CS 챗봇이다. 아래 [FAQ 데이터]를 바탕으로 답변해라.\n\n");
        sb.append("[FAQ 데이터]\n");
        for(FaqResponse faq : faqs) {
            sb.append("- 질문 : ").append(faq.getQuestion())
              .append(" / 답변: ").append(faq.getAnswer()).append("\n");
        }
        return sb.toString();
    }

    // Fallback 응답 생성 및 이력 저장 메서드
    private ChatResponse createFallbackResponse(String sessionId, String userQuery) {
        String fallbackMsg = "죄송합니다. 요청하신 질문에 관련된 FAQ를 찾을 수 없거나 AI 서비스 연동이 지연되고 있습니다."
                + " 정확한 안내를 위해 1:1 문의 접수를 진행해 주세요.";

        searchHistoryService.saveSearchHistory(userQuery, "[Fallback] " + fallbackMsg);

        return ChatResponse.builder()
                .sessionId(sessionId)
                .aiResponse(fallbackMsg)
                .referencedFaqs(List.of())
                .isFallback(true)
                .build();
    }
}
