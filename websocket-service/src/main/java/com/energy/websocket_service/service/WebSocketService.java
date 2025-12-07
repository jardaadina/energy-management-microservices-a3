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
    /**
     * Send overconsumption alert to specific user
     */
    public void sendAlert(String userId, OverconsumptionAlert alert) {
        try {
            log.info("Sending alert to user: {}", userId);

            // Create notification wrapper
            NotificationMessage notification = new NotificationMessage(
                    "ALERT",
                    "Overconsumption Warning",
                    alert.getMessage(),
                    LocalDateTime.now(),
                    alert
            );

            // FIX: Trimitem pe topic public cu ID-ul userului
            String destination = "/topic/user/" + userId + "/alerts";
            messagingTemplate.convertAndSend(destination, notification);
            messagingTemplate.convertAndSend("/topic/user/admin/alerts", alert);
            log.info("Alert sent successfully to: {}", destination);

        } catch (Exception e) {
            log.error("Error sending alert to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Send chat message to specific user
     */
    public void sendChatMessage(String recipientId, ChatMessage message) {
        log.info("Sending chat message to recipient: {}", recipientId);

        // Dacă destinatarul este admin
        if ("admin".equals(recipientId)) {
            // Încercăm să trimitem
            messagingTemplate.convertAndSend("/topic/user/admin/messages", message);

            // DAR, pentru siguranță, salvăm și în buffer dacă adminul nu a confirmat prezența
            // (Notă: Într-un sistem real am verifica sesiunile active, aici simplificăm)
            addToOfflineBuffer("admin", message);
        } else {
            // Mesaj către user normal
            messagingTemplate.convertAndSend("/topic/user/" + recipientId + "/messages", message);
        }
    }

    // Metodă apelată când Adminul dă JOIN
    public void notifyAdminConnected() {
        log.info("Admin connected! Replaying offline messages...");
        isAdminConnected = true;

        List<ChatMessage> pending = offlineMessages.get("admin");
        if (pending != null && !pending.isEmpty()) {
            for (ChatMessage msg : pending) {
                messagingTemplate.convertAndSend("/topic/user/admin/messages", msg);
            }
            // Curățăm bufferul după trimitere
            pending.clear();
        }
    }

    private void addToOfflineBuffer(String userId, ChatMessage message) {
        offlineMessages.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
        // Limităm bufferul la ultimele 50 mesaje ca să nu explodeze memoria
        if (offlineMessages.get(userId).size() > 50) {
            offlineMessages.get(userId).remove(0);
        }
    }
}
