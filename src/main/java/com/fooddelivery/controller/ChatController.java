package com.fooddelivery.controller;

import com.fooddelivery.service.chat.ChatOrchestratorService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatOrchestratorService chatOrchestratorService;

    public ChatController(ChatOrchestratorService chatOrchestratorService) {
        this.chatOrchestratorService = chatOrchestratorService;
    }

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> body, Authentication authentication) {
        String message = body.get("message");
        String userId = authentication.getName();
        String reply = chatOrchestratorService.chat(userId, message);
        return Map.of("reply", reply);
    }

    @DeleteMapping
    public void resetConversation(Authentication authentication) {
        chatOrchestratorService.resetConversation(authentication.getName());
    }
}