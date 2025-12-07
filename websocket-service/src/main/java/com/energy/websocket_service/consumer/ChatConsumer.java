package com.energy.websocket_service.consumer;

import com.energy.websocket_service.dto.ChatMessage;
import com.energy.websocket_service.service.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatConsumer {

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "${rabbitmq.queue.chat-messages}")
    public void handleChatMessage(String message) {
        try {
            log.info("Received chat message: {}", message);

            // Parse the chat message
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);

            // Send to recipient via WebSocket
            webSocketService.sendChatMessage(chatMessage.getRecipientId(), chatMessage);

            log.info("Chat message sent from {} to {}",
                    chatMessage.getSenderId(), chatMessage.getRecipientId());

        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);
        }
    }
}
