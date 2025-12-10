package com.energy.websocket_service.controller;

import com.energy.websocket_service.dto.ChatMessage;
import com.energy.websocket_service.service.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Controller
public class WebSocketController {

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange.chat}")
    private String chatExchange;

    @Value("${rabbitmq.routing-key.chat}")
    private String chatRoutingKey;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("Received chat message from: {} to: {}", message.getSenderId(), message.getRecipientId());

            message.setTimestamp(LocalDateTime.now());
            if (message.getMessageId() == null) {
                message.setMessageId(UUID.randomUUID().toString());
            }

            if ("admin".equals(message.getSenderId()) || "ADMIN".equals(message.getSenderRole())) {
                log.info("Admin responding directly to user {}", message.getRecipientId());
                webSocketService.sendChatMessage(message.getRecipientId(), message);
                return;
            }

            String jsonMessage = objectMapper.writeValueAsString(message);

            rabbitTemplate.convertAndSend(chatExchange, chatRoutingKey, jsonMessage);

            log.info("Chat message forwarded to Customer Support Service: {}", jsonMessage);

        } catch (Exception e) {
            log.error("Error handling chat message: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload ChatMessage message) {
        try {
            message.setType(ChatMessage.MessageType.TYPING);
            message.setTimestamp(LocalDateTime.now());
            webSocketService.sendChatMessage(message.getRecipientId(), message);
        } catch (Exception e) {
            log.error("Error handling typing indicator: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.join")
    public void handleJoin(@Payload ChatMessage message) {
        try {
            log.info("User {} joined chat", message.getSenderId());
            message.setType(ChatMessage.MessageType.JOIN);
            message.setTimestamp(LocalDateTime.now());
            message.setContent(message.getSenderId() + " joined the chat");

            if ("admin".equals(message.getSenderId())) {
                log.info("Admin JOIN detected via WebSocket");
                webSocketService.notifyAdminConnected();
            }

            if ("admin".equals(message.getRecipientId())) {
                webSocketService.sendChatMessage("admin", message);
            }
        } catch (Exception e) {
            log.error("Error handling user join: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.leave")
    public void handleLeave(@Payload ChatMessage message) {
        try {
            log.info("User {} left chat", message.getSenderId());
            message.setType(ChatMessage.MessageType.LEAVE);
            message.setTimestamp(LocalDateTime.now());
            message.setContent(message.getSenderId() + " left the chat");

            if ("admin".equals(message.getRecipientId())) {
                webSocketService.sendChatMessage("admin", message);
            }
        } catch (Exception e) {
            log.error("Error handling user leave: {}", e.getMessage(), e);
        }
    }
}