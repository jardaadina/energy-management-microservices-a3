package com.energy.device.service;

import com.energy.device.dto.DeviceDTO;

import java.util.List;

public interface UserDeviceService {
    void assignDeviceToUser(Long userId, Long deviceId);
    void unassignDeviceFromUser(Long userId, Long deviceId);
    List<DeviceDTO> getDevicesByUserId(Long userId);
    void deleteAssignmentsByUserId(Long userId);
}