package com.energy.device.controller;

import com.energy.device.dto.CreateDeviceRequest;
import com.energy.device.dto.DeviceDTO;
import com.energy.device.dto.UpdateDeviceRequest;
import com.energy.device.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final DeviceService deviceService;

    // CREATE - POST http://localhost:8082/devices
    @PostMapping
    public ResponseEntity<DeviceDTO> createDevice(@Valid @RequestBody CreateDeviceRequest request) {
        log.info("REST request to create device: {}", request.getName());
        DeviceDTO device = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }

    // READ ONE - GET http://localhost:8082/devices/1
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable Long id) {
        log.info("REST request to get device: {}", id);
        DeviceDTO device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

    // READ ALL - GET http://localhost:8082/devices
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getAllDevices() {
        log.info("REST request to get all devices");
        List<DeviceDTO> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }

    // UPDATE - PUT http://localhost:8082/devices/1
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeviceRequest request) {
        log.info("REST request to update device: {}", id);
        DeviceDTO device = deviceService.updateDevice(id, request);
        return ResponseEntity.ok(device);
    }

    // DELETE - DELETE http://localhost:8082/devices/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        log.info("REST request to delete device: {}", id);
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}