package com.energy.device.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeviceDTO {
    private Long id;
    private Long userId;
    private Long deviceId;
    private DeviceDTO device;
}