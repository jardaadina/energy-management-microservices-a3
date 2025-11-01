package com.energy.device.mapper;

import com.energy.device.dto.CreateDeviceRequest;
import com.energy.device.dto.DeviceDTO;
import com.energy.device.dto.UpdateDeviceRequest;
import com.energy.device.entity.Device;

public class DeviceMapper {

    public static DeviceDTO toDTO(Device device)
    {
        if (device == null) {
            return null;
        }

        return DeviceDTO.builder()
                .id(device.getId())
                .name(device.getName())
                .maxConsumption(device.getMaxConsumption())
                .build();
    }

    public static Device toEntity(CreateDeviceRequest request)
    {
        if (request == null) {
            return null;
        }

        return Device.builder()
                .name(request.getName())
                .maxConsumption(request.getMaxConsumption())
                .build();
    }

    public static void updateEntity(Device device, UpdateDeviceRequest request)
    {
        // Actualizăm doar câmpurile care nu sunt null
        if (request.getName() != null) {
            device.setName(request.getName());
        }
        if (request.getMaxConsumption() != null) {
            device.setMaxConsumption(request.getMaxConsumption());
        }
    }
}