package com.energy.monitoring.consumer;

import com.energy.monitoring.dto.SyncEvent;
import com.energy.monitoring.service.SyncConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncEventConsumer {

    private final SyncConsumerService syncService;

    @RabbitListener(queues = "${rabbitmq.queue.sync}.monitoring")
    public void consumeSyncEvent(SyncEvent event) {
        log.info("Received sync event: type={}, entityId={}",
                event.getEventType(), event.getEntityId());

        try {
            syncService.handleSyncEvent(event);
            log.info("Successfully processed sync event: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Error processing sync event: {}", e.getMessage(), e);
        }
    }
}