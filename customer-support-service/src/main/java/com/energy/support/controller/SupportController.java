package com.energy.support.controller;

import com.energy.support.dto.AdminChatMessage;
import com.energy.support.dto.ChatRequest;
import com.energy.support.dto.ChatResponse;
import com.energy.support.model.ChatRule;
import com.energy.support.service.ChatService;
import com.energy.support.service.RuleBasedChatbotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/support")
@CrossOrigin(origins = "*")
public class SupportController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private RuleBasedChatbotService ruleBasedService;

    /**
     * Test endpoint for direct chat (without WebSocket)
     */
    @PostMapping("/chat")
    public Mono<ResponseEntity<ChatResponse>> sendMessage(@RequestBody ChatRequest request) {
        log.info("Received chat message via REST: {}", request.getMessage());

        return chatService.handleMessage(request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * Admin sends message to user
     */
    @PostMapping("/admin/send")
    public ResponseEntity<Map<String, String>> adminSendMessage(@RequestBody AdminChatMessage message) {
        log.info("Admin {} sending message to user {}", message.getAdminId(), message.getUserId());

        chatService.handleAdminResponse(
                message.getAdminId(),
                message.getUserId(),
                message.getMessage()
        );

        Map<String, String> response = new HashMap<>();
        response.put("status", "sent");
        response.put("message", "Message delivered to user");

        return ResponseEntity.ok(response);
    }

    /**
     * Get all chatbot rules (for admin/debugging)
     */
    @GetMapping("/rules")
    public ResponseEntity<List<ChatRule>> getRules() {
        List<ChatRule> rules = ruleBasedService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * Get chatbot statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRules", ruleBasedService.getRuleCount());
        stats.put("status", "operational");
        stats.put("version", "1.0.0");

        return ResponseEntity.ok(stats);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "customer-support-service");

        return ResponseEntity.ok(health);
    }
}