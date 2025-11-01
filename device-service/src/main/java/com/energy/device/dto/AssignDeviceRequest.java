package com.energy.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignDeviceRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Device ID is required")
    private Long deviceId;
}