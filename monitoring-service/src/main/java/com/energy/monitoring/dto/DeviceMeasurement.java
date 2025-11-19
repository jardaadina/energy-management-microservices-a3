package com.energy.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceMeasurement {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private Long deviceId;

    private Double measurementValue;
}