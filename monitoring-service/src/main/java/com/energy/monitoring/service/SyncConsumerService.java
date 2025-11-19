package com.energy.monitoring.service;

import com.energy.monitoring.dto.SyncEvent;
import com.energy.monitoring.entity.DeviceReference;
import com.energy.monitoring.entity.UserReference;
import com.energy.monitoring.repository.DeviceReferenceRepository;
import com.energy.monitoring.repository.UserReferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncConsumerService {

    private final UserReferenceRepository userReferenceRepository;
    private final DeviceReferenceRepository deviceReferenceRepository;

    @Transactional
    public void handleSyncEvent(SyncEvent event) {
        switch (event.getEventType()) {
            case "USER_CREATED":
                handleUserCreated(event);
                break;
            case "DEVICE_CREATED":
                handleDeviceCreated(event);
                break;
            default:
                log.warn("Unknown sync event type: {}", event.getEventType());
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
        log.info("User reference saved successfully");
    }

    private void handleDeviceCreated(SyncEvent event) {
        log.info("Syncing device: deviceId={}, deviceName={}, maxConsumption={}",
                event.getEntityId(), event.getDeviceName(), event.getMaxConsumption());

        DeviceReference deviceRef = DeviceReference.builder()
                .deviceId(event.getEntityId())
                .deviceName(event.getDeviceName())
                .maxConsumption(event.getMaxConsumption())
                .build();

        deviceReferenceRepository.save(deviceRef);
        log.info("Device reference saved successfully in monitoring-service");
    }
}