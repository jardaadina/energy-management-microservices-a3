package com.energy.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDeviceRequest {

    @NotBlank(message = "Device name is required")
    private String name;

    @NotNull(message = "Max consumption is required")
    @Positive(message = "Max consumption must be positive")
    private Double maxConsumption;
}