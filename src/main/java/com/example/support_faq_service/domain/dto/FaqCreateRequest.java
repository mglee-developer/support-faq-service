package com.example.support_faq_service.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FaqCreateRequest {
    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;
    @NotBlank(message = "질문은 필수입니다.")
    private String question;
    @NotBlank(message = "답변은 필수입니다.")
    private String answer;
}
