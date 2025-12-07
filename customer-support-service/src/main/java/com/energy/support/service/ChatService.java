package com.energy.support.service;

import com.energy.support.dto.ChatRequest;
import com.energy.support.dto.ChatResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ChatService {

    @Autowired
    private RuleBasedChatbotService ruleBasedService;

    @Autowired
    private AIService aiService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange.chat}")
    private String chatExchange;

    @Value("${rabbitmq.routing-key.chat-response}")
    private String chatResponseRoutingKey;

    @Value("${chatbot.fallback-to-ai:true}")
    private boolean fallbackToAI;

    /**
     * Process incoming chat message
     * 1. Try rule-based response
     * 2. If no match and AI enabled, use AI
     * 3. Forward to admin if needed
     */
    public Mono<ChatResponse> handleMessage(ChatRequest request) {
        log.info("Processing chat message from user: {}", request.getUserId());

        String messageLower = request.getMessage().toLowerCase();

        // --- MODIFICARE: Bypass pentru Admin ---
        // Dacă utilizatorul cere explicit admin, ignorăm AI-ul și regulile
        if (messageLower.contains("admin") || messageLower.contains("human") || messageLower.contains("help")) {
            log.info("User requested admin explicitly");

            // Trimitem notificarea către Admin Panel
            notifyAdmin(request);

            // Răspundem utilizatorului că se face legătura
            ChatResponse systemResponse = new ChatResponse(
                    "I have forwarded your request to a human administrator. They will chat with you shortly.",
                    "FORWARDED_TO_ADMIN"
            );
            sendResponseToWebSocket(request.getUserId(), systemResponse);
            return Mono.just(systemResponse);
        }
        // ---------------------------------------

        // 1. First, try rule-based response
        ChatResponse ruleResponse = ruleBasedService.processMessage(request.getMessage());
        if (ruleResponse != null) {
            sendResponseToWebSocket(request.getUserId(), ruleResponse);
            return Mono.just(ruleResponse);
        }

        // 2. No rule matched - try AI
        if (fallbackToAI && aiService.isEnabled()) {
            // ... restul codului AI ...
            return aiService.generateAIResponse(request.getMessage())
                    .doOnSuccess(aiResponse -> {
                        sendResponseToWebSocket(request.getUserId(), aiResponse);
                    });
        }

        // 3. Fallback to Admin (dacă AI e oprit)
        log.info("No rule matched and AI disabled, forwarding to admin");
        notifyAdmin(request);
        // ... restul codului ...
        return Mono.empty(); // sau returnează response-ul de admin
    }

    /**
     * Send response back to user via WebSocket (through RabbitMQ)
     */
    private void sendResponseToWebSocket(String userId, ChatResponse response) {
        try {
            response.setMessageId(UUID.randomUUID().toString());
            response.setTimestamp(LocalDateTime.now());

            // Create message payload for WebSocket
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("messageId", response.getMessageId());
            messagePayload.put("senderId", "support-bot");
            messagePayload.put("recipientId", userId);
            messagePayload.put("content", response.getMessage());
            messagePayload.put("senderRole", "ADMIN");
            messagePayload.put("timestamp", response.getTimestamp().toString());
            messagePayload.put("type", "CHAT");

            String jsonMessage = objectMapper.writeValueAsString(messagePayload);

            rabbitTemplate.convertAndSend(
                    chatExchange,
                    chatResponseRoutingKey,
                    jsonMessage
            );

            log.info("Response sent to WebSocket for user: {}", userId);

        } catch (JsonProcessingException e) {
            log.error("Error serializing chat response: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error sending response to WebSocket: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify admin about new user message
     */
    private void notifyAdmin(ChatRequest request) {
        try {
            Map<String, Object> adminNotification = new HashMap<>();
            adminNotification.put("userId", request.getUserId());
            adminNotification.put("message", request.getMessage());
            adminNotification.put("timestamp", LocalDateTime.now().toString());
            adminNotification.put("type", "NEW_USER_MESSAGE");

            String jsonMessage = objectMapper.writeValueAsString(adminNotification);

            // Send to admin's queue
            rabbitTemplate.convertAndSend(
                    chatExchange,
                    "chat.admin.notification",
                    jsonMessage
            );

            log.info("Admin notified about message from user: {}", request.getUserId());

        } catch (Exception e) {
            log.error("Error notifying admin: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle admin response to user
     */
    public void handleAdminResponse(String adminId, String userId, String message) {
        try {
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("messageId", UUID.randomUUID().toString());
            messagePayload.put("senderId", adminId);
            messagePayload.put("recipientId", userId);
            messagePayload.put("content", message);
            messagePayload.put("senderRole", "ADMIN");
            messagePayload.put("timestamp", LocalDateTime.now().toString());
            messagePayload.put("type", "CHAT");

            String jsonMessage = objectMapper.writeValueAsString(messagePayload);

            rabbitTemplate.convertAndSend(
                    chatExchange,
                    chatResponseRoutingKey,
                    jsonMessage
            );

            log.info("Admin response sent to user: {}", userId);

        } catch (Exception e) {
            log.error("Error sending admin response: {}", e.getMessage(), e);
        }
    }
}