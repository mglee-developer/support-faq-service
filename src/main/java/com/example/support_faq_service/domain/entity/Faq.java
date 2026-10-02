package com.example.support_faq_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "faq")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Faq {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String answer;

    @Column(nullable = false)
    @Builder.Default    // Builder 사용 시 기본 값 0 세팅
    private int viewCnt;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Faq(
            String category,
            String question,
            String answer,
            Integer viewCnt
    ) {
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.viewCnt = viewCnt;
    }

    // 조회수 증가 비즈니스 메서드
    public void increaseViewCnt() {
        this.viewCnt++;
    }
}
