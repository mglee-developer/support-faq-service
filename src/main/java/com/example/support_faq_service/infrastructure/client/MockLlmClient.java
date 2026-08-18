package com.example.support_faq_service.infrastructure.client;

import org.springframework.stereotype.Service;

@Service
public class MockLlmClient implements LlmClient {

    @Override
    public String generateAnswer(String systemPrompt, String userQuery) {
        // 1. 테스트용 에러 분기
        // 나중에 fallback(1:1 문의 안내) 로직을 테스트하기 위해
        // 사용자가 "에러"라고 질문하면 강제로 예외를 발생시켜 봅니다.
        if(userQuery.contains("에러")) {
            throw new RuntimeException("OpenAI API 연동 장애가 발생했습니다. (Mock Error)");
        }

        // 가짜 AI 응답 생성
        // 실제 API 호출 없이, 질문 애용을 재활용해서 마치 AI가 답변한 것처럼 만들어 줍니다.
        return "[AI 응답 완료] 문의하신 '" + userQuery + "'에 대한 답변입니다."
                + " 제시된 FAQ 내용을 바탕으로 안내해 드립니다.";


    }
}
