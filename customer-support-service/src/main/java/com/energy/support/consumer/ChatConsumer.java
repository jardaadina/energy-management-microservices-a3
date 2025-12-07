package com.energy.support.consumer;

import com.energy.support.dto.ChatRequest;
import com.energy.support.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class ChatConsumer {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "${rabbitmq.queue.chat-requests}")
    public void handleChatRequest(String message) {
        try {
            log.info("Received chat request: {}", message);

            // Parse the incoming message
            JsonNode jsonNode = objectMapper.readTree(message);

            ChatRequest request = new ChatRequest();
            request.setMessageId(jsonNode.path("messageId").asText());
            request.setUserId(jsonNode.path("senderId").asText());
            request.setMessage(jsonNode.path("content").asText());

            // Parse timestamp
            String timestampStr = jsonNode.path("timestamp").asText();
            if (timestampStr != null && !timestampStr.isEmpty()) {
                request.setTimestamp(LocalDateTime.parse(timestampStr));
            } else {
                request.setTimestamp(LocalDateTime.now());
            }

            // Process the message
            chatService.handleMessage(request).subscribe(
                    response -> log.info("Chat response generated: {}", response.getResponseType()),
                    error -> log.error("Error processing chat message: {}", error.getMessage())
            );

        } catch (Exception e) {
            log.error("Error handling chat request: {}", e.getMessage(), e);
        }
    }
}