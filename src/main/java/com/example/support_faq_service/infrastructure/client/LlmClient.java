package com.example.support_faq_service.infrastructure.client;

public interface LlmClient {
    // 프롬프트와 사용자 질문을 받아서 답변 문자열을 반환
    String generateAnswer(String systemPrompt, String userQuery);
}
