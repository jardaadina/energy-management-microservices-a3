package com.energy.device.service.impl;

import com.energy.device.dto.DeviceDTO;
import com.energy.device.entity.Device;
import com.energy.device.entity.UserDevice;
import com.energy.device.exception.ResourceAlreadyExistsException;
import com.energy.device.exception.ResourceNotFoundException;
import com.energy.device.mapper.DeviceMapper;
import com.energy.device.repository.DeviceRepository;
import com.energy.device.repository.UserDeviceRepository;
import com.energy.device.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeviceServiceImpl implements UserDeviceService
{

    private final UserDeviceRepository userDeviceRepository;
    private final DeviceRepository deviceRepository;

    @Override
    @Transactional
    public void assignDeviceToUser(Long userId, Long deviceId) {
        log.info("Assigning device {} to user {}", deviceId, userId);

        if (!deviceRepository.existsById(deviceId))
        {
            throw new ResourceNotFoundException("Device not found with id: " + deviceId);
        }

        if (userDeviceRepository.existsByUserIdAndDeviceId(userId, deviceId))
        {
            throw new ResourceAlreadyExistsException("Device already assigned to this user");
        }

        UserDevice userDevice = UserDevice.builder()
                .userId(userId)
                .deviceId(deviceId)
                .build();

        userDeviceRepository.save(userDevice);
        log.info("Device assigned successfully");
    }

    @Override
    @Transactional
    public void unassignDeviceFromUser(Long userId, Long deviceId)
    {
        log.info("Unassigning device {} from user {}", deviceId, userId);

        if (!userDeviceRepository.existsByUserIdAndDeviceId(userId, deviceId))
        {
            throw new ResourceNotFoundException("Assignment not found");
        }

        userDeviceRepository.deleteByUserIdAndDeviceId(userId, deviceId);
        log.info("Device unassigned successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceDTO> getDevicesByUserId(Long userId)
    {
        log.info("Fetching devices for user: {}", userId);

        List<UserDevice> userDevices = userDeviceRepository.findByUserId(userId);

        return userDevices.stream()
                .map(ud -> {
                    Device device = deviceRepository.findById(ud.getDeviceId())
                            .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
                    return DeviceMapper.toDTO(device);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAssignmentsByUserId(Long userId) {
        log.info("Deleting all assignments for user id: {}", userId);
        userDeviceRepository.deleteByUserId(userId);
        log.info("Assignments deleted for user id: {}", userId);
    }
}