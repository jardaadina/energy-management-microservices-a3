package com.energy.websocket_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverconsumptionAlert {
    private Long deviceId;
    private Long userId;
    private String deviceName;
    private Double currentConsumption;
    private Double maxConsumption;
    private LocalDateTime timestamp;
    private String message;
}
