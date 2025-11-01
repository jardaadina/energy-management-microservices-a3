package com.energy.device.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDeviceRequest {

    private String name;

    @Positive(message = "Max consumption must be positive")
    private Double maxConsumption;

}