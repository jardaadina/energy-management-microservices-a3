package com.energy.device.controller;

import com.energy.device.dto.AssignDeviceRequest;
import com.energy.device.dto.DeviceDTO;
import com.energy.device.service.UserDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-devices")
@RequiredArgsConstructor
@Slf4j
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    // ASSIGN - POST http://localhost:8082/user-devices/assign
    // Body: {"userId": 1, "deviceId": 1}
    @PostMapping("/assign")
    public ResponseEntity<String> assignDevice(@Valid @RequestBody AssignDeviceRequest request) {
        log.info("REST request to assign device {} to user {}",
                request.getDeviceId(), request.getUserId());
        userDeviceService.assignDeviceToUser(request.getUserId(), request.getDeviceId());
        return ResponseEntity.ok("Device assigned successfully");
    }

    // UNASSIGN - DELETE http://localhost:8082/user-devices/unassign
    // Body: {"userId": 1, "deviceId": 1}
    @DeleteMapping("/unassign")
    public ResponseEntity<String> unassignDevice(@Valid @RequestBody AssignDeviceRequest request) {
        log.info("REST request to unassign device {} from user {}",
                request.getDeviceId(), request.getUserId());
        userDeviceService.unassignDeviceFromUser(request.getUserId(), request.getDeviceId());
        return ResponseEntity.ok("Device unassigned successfully");
    }

    // GET USER'S DEVICES - GET http://localhost:8082/user-devices/user/1
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDTO>> getUserDevices(@PathVariable Long userId) {
        log.info("REST request to get devices for user: {}", userId);
        List<DeviceDTO> devices = userDeviceService.getDevicesByUserId(userId);
        return ResponseEntity.ok(devices);
    }

    @DeleteMapping("/by-user/{userId}")
    public ResponseEntity<Void> deleteAssignmentsForUser(@PathVariable Long userId) {
        userDeviceService.deleteAssignmentsByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}