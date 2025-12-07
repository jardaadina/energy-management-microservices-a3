package com.energy.websocket_service.consumer;

import com.energy.websocket_service.dto.ChatMessage;
import com.energy.websocket_service.service.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class AdminConsumer {

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private ObjectMapper objectMapper;

    // Ascultă coada definită la Pasul 1
    @RabbitListener(queues = "chat-admin-notifications")
    public void handleAdminNotification(String message) {
        try {
            log.info("Received admin notification: {}", message);

            // Mesajul vine ca un Map simplu din ChatService (vezi notifyAdmin)
            // Structura: {userId=..., message=..., timestamp=..., type=...}
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);

            // Convertim în formatul așteptat de Frontend (ChatMessage)
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderId((String) payload.get("userId"));
            chatMessage.setRecipientId("admin");
            chatMessage.setContent((String) payload.get("message"));
            chatMessage.setSenderRole("USER");
            chatMessage.setTimestamp(java.time.LocalDateTime.now());
            chatMessage.setType(ChatMessage.MessageType.CHAT);

            // Trimitem mesajul specific către admin
            // Adminul este abonat la /topic/user/admin/messages
            webSocketService.sendChatMessage("admin", chatMessage);

            log.info("Forwarded user message to Admin Panel: {}", chatMessage.getContent());

        } catch (Exception e) {
            log.error("Error processing admin notification: {}", e.getMessage(), e);
        }
    }
}