package com.energy.device.mapper;

import com.energy.device.dto.UserDeviceDTO;
import com.energy.device.entity.UserDevice;

public class UserDeviceMapper {

    public static UserDeviceDTO toDTO(UserDevice userDevice) {
        if (userDevice == null) {
            return null;
        }

        return UserDeviceDTO.builder()
                .id(userDevice.getId())
                .userId(userDevice.getUserId())
                .deviceId(userDevice.getDeviceId())
                .build();
    }

    public static UserDeviceDTO toDTOWithDevice(UserDevice userDevice) {
        if (userDevice == null) {
            return null;
        }

        UserDeviceDTO dto = UserDeviceDTO.builder()
                .id(userDevice.getId())
                .userId(userDevice.getUserId())
                .deviceId(userDevice.getDeviceId())
                .build();

        if (userDevice.getDevice() != null) {
            dto.setDevice(DeviceMapper.toDTO(userDevice.getDevice()));
        }

        return dto;
    }

    public static UserDevice toEntity(Long userId, Long deviceId) {
        return UserDevice.builder()
                .userId(userId)
                .deviceId(deviceId)
                .build();
    }
}