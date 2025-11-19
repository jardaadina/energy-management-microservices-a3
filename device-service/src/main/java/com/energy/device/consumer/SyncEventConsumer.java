package com.energy.device.consumer;

import com.energy.device.dto.SyncEvent;
import com.energy.device.entity.UserReference;
import com.energy.device.repository.UserReferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncEventConsumer {

    private final UserReferenceRepository userReferenceRepository;

    @RabbitListener(queues = "${rabbitmq.queue.sync}.device")
    public void consumeSyncEvent(SyncEvent event) {
        log.info("Received sync event: type={}, entityId={}",
                event.getEventType(), event.getEntityId());

        try {
            if ("USER_CREATED".equals(event.getEventType())) {
                handleUserCreated(event);
            } else if ("DEVICE_CREATED".equals(event.getEventType())) {
                log.info("Device created event received: deviceId={}", event.getEntityId());
            }
        } catch (Exception e) {
            log.error("Error processing sync event: {}", e.getMessage(), e);
        }
    }

    private void handleUserCreated(SyncEvent event) {
        log.info("Syncing user: userId={}, username={}",
                event.getEntityId(), event.getUsername());

        UserReference userRef = UserReference.builder()
                .userId(event.getEntityId())
                .username(event.getUsername())
                .build();

        userReferenceRepository.save(userRef);
        log.info("User reference saved successfully in device-service");
    }
}