package com.energy.websocket_service.consumer;

import com.energy.websocket_service.dto.OverconsumptionAlert;
import com.energy.websocket_service.service.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "${rabbitmq.queue.overconsumption}")
    public void handleOverconsumptionAlert(String message) {
        try {
            log.info("Received overconsumption alert: {}", message);

            OverconsumptionAlert alert = objectMapper.readValue(message, OverconsumptionAlert.class);

            webSocketService.sendAlert(alert.getUserId().toString(), alert);

            log.info("Alert sent to user {} for device {}", alert.getUserId(), alert.getDeviceId());

        } catch (Exception e) {
            log.error("Error processing overconsumption alert: {}", e.getMessage(), e);
        }
    }
}