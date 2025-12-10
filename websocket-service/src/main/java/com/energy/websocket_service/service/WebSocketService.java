package com.energy.websocket_service.service;

import com.energy.websocket_service.dto.ChatMessage;
import com.energy.websocket_service.dto.NotificationMessage;
import com.energy.websocket_service.dto.OverconsumptionAlert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private final Map<String, List<ChatMessage>> offlineMessages = new ConcurrentHashMap<>();

    private boolean isAdminConnected = false;

    public void sendAlert(String userId, OverconsumptionAlert alert) {
        try {
            log.info("Sending alert to user: {}", userId);

            NotificationMessage notification = new NotificationMessage(
                    "ALERT",
                    "Overconsumption Warning",
                    alert.getMessage(),
                    LocalDateTime.now(),
                    alert
            );

            String destination = "/topic/user/" + userId + "/alerts";
            messagingTemplate.convertAndSend(destination, notification);
            messagingTemplate.convertAndSend("/topic/user/admin/alerts", alert);
            log.info("Alert sent successfully to: {}", destination);

        } catch (Exception e) {
            log.error("Error sending alert to user {}: {}", userId, e.getMessage(), e);
        }
    }

    public void sendChatMessage(String recipientId, ChatMessage message) {
        log.info("Sending chat message to recipient: {}", recipientId);

        if ("admin".equals(recipientId)) {
            messagingTemplate.convertAndSend("/topic/user/admin/messages", message);

            addToOfflineBuffer("admin", message);
        } else {
            messagingTemplate.convertAndSend("/topic/user/" + recipientId + "/messages", message);
        }
    }

    public void notifyAdminConnected() {
        log.info("Admin connected! Replaying offline messages...");
        isAdminConnected = true;

        List<ChatMessage> pending = offlineMessages.get("admin");
        if (pending != null && !pending.isEmpty()) {
            for (ChatMessage msg : pending) {
                messagingTemplate.convertAndSend("/topic/user/admin/messages", msg);
            }
            pending.clear();
        }
    }

    private void addToOfflineBuffer(String userId, ChatMessage message) {
        offlineMessages.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
        if (offlineMessages.get(userId).size() > 50) {
            offlineMessages.get(userId).remove(0);
        }
    }
}
