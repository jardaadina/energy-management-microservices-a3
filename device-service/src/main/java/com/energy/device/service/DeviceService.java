package com.energy.device.service;

import com.energy.device.dto.CreateDeviceRequest;
import com.energy.device.dto.DeviceDTO;
import com.energy.device.dto.UpdateDeviceRequest;

import java.util.List;

public interface DeviceService {
    DeviceDTO createDevice(CreateDeviceRequest request);
    DeviceDTO getDeviceById(Long id);
    List<DeviceDTO> getAllDevices();
    DeviceDTO updateDevice(Long id, UpdateDeviceRequest request);
    void deleteDevice(Long id);
}