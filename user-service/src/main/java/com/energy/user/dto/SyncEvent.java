package com.energy.user.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncEvent {
    private String eventType;
    private Long entityId;
    private String username;
    private String deviceName;
    private Double maxConsumption;
}