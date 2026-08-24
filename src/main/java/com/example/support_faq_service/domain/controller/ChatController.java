package com.example.support_faq_service.domain.controller;

import com.example.support_faq_service.domain.dto.ChatRequest;
import com.example.support_faq_service.domain.dto.ChatResponse;
import com.example.support_faq_service.domain.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    // RAG AI 챗봇 대화 요청
    @PostMapping
    public ResponseEntity<ChatResponse> processChat(@Valid @RequestBody ChatRequest chatRequest) {
        ChatResponse chatResponse = chatService.processChat(chatRequest);
        return ResponseEntity.ok(chatResponse);
    }
}
