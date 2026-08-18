package com.example.support_faq_service.domain.dto;

import com.example.support_faq_service.domain.entity.Faq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
public class FaqResponse {
    private Long id;
    private String category;
    private String question;
    private String answer;
    private int viewCnt;
    private LocalDateTime createdAt;

    public FaqResponse(Faq faq) {
        this.id = faq.getId();
        this.category = faq.getCategory();
        this.question = faq.getQuestion();
        this.answer = faq.getAnswer();
        this.viewCnt = faq.getViewCnt();
        this.createdAt = faq.getCreatedAt();
    }
}
