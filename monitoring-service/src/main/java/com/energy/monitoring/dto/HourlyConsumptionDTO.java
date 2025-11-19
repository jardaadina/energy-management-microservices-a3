package com.energy.monitoring.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyConsumptionDTO {
    private Long id;
    private Long deviceId;
    private LocalDateTime timestamp;
    private Double totalConsumption;
}