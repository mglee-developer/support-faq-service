package com.example.support_faq_service.common.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final String errorCode;
    private final String message;
    private final LocalDateTime timestamp;
}
