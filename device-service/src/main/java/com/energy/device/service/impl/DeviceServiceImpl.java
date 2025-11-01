package com.energy.device.service.impl;

import com.energy.device.dto.CreateDeviceRequest;
import com.energy.device.dto.DeviceDTO;
import com.energy.device.dto.UpdateDeviceRequest;
import com.energy.device.entity.Device;
import com.energy.device.exception.ResourceNotFoundException;
import com.energy.device.mapper.DeviceMapper;
import com.energy.device.repository.DeviceRepository;
import com.energy.device.repository.UserDeviceRepository;
import com.energy.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceServiceImpl implements DeviceService
{

    private final DeviceRepository deviceRepository;
    private final UserDeviceRepository userDeviceRepository;

    @Override
    @Transactional
    public DeviceDTO createDevice(CreateDeviceRequest request)
    {
        log.info("Creating device: {}", request.getName());

        Device device = DeviceMapper.toEntity(request);
        Device savedDevice = deviceRepository.save(device);
        return DeviceMapper.toDTO(savedDevice);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceDTO getDeviceById(Long id)
    {
        log.info("Fetching device with id: {}", id);

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
        return DeviceMapper.toDTO(device);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceDTO> getAllDevices()
    {
        log.info("Fetching all devices");

        return deviceRepository.findAll()
                .stream()
                .map(DeviceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeviceDTO updateDevice(Long id, UpdateDeviceRequest request)
    {
        log.info("Updating device with id: {}", id);

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        DeviceMapper.updateEntity(device, request);
        Device updatedDevice = deviceRepository.save(device);
        return DeviceMapper.toDTO(updatedDevice);
    }

    @Override
    @Transactional
    public void deleteDevice(Long id)
    {
        log.info("Deleting device with id: {}", id);

        if (!deviceRepository.existsById(id))
        {
            throw new ResourceNotFoundException("Device not found with id: " + id);
        }

        log.info("Deleting assignments for device id: {}", id);
        userDeviceRepository.deleteByDeviceId(id);

        deviceRepository.deleteById(id);
        log.info("Device and its assignments deleted successfully for id: {}", id);
    }
}