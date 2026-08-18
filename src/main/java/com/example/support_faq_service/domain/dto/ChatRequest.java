package com.example.support_faq_service.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    @NotBlank(message = "세션 ID는 필수입니다.")
    private String sessionId;
    @NotBlank(message = "질문 내용을 입력해주세요.")
    private String userQuery;
}
