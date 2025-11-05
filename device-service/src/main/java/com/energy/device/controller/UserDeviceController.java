package com.energy.device.controller;

import com.energy.device.dto.AssignDeviceRequest;
import com.energy.device.dto.DeviceDTO;
import com.energy.device.service.UserDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@RestController
@RequestMapping("/user-devices")
@RequiredArgsConstructor
@Slf4j
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    private void checkAdmin(String role)
    {
        if (!"ADMIN".equalsIgnoreCase(role))
        {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access Denied: Requires ADMIN role.");
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignDevice(@Valid @RequestBody AssignDeviceRequest request,
                                               @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to assign device {} to user {}",
                request.getDeviceId(), request.getUserId());
        userDeviceService.assignDeviceToUser(request.getUserId(), request.getDeviceId());
        return ResponseEntity.ok("Device assigned successfully");
    }

    @DeleteMapping("/unassign")
    public ResponseEntity<String> unassignDevice(@Valid @RequestBody AssignDeviceRequest request,
                                                 @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to unassign device {} from user {}",
                request.getDeviceId(), request.getUserId());
        userDeviceService.unassignDeviceFromUser(request.getUserId(), request.getDeviceId());
        return ResponseEntity.ok("Device unassigned successfully");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDTO>> getUserDevices(@PathVariable Long userId,
                                                          @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to get devices for user: {}", userId);
        List<DeviceDTO> devices = userDeviceService.getDevicesByUserId(userId);
        return ResponseEntity.ok(devices);
    }

    @DeleteMapping("/by-user/{userId}")
    public ResponseEntity<Void> deleteAssignmentsForUser(@PathVariable Long userId)
    {
        userDeviceService.deleteAssignmentsByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-devices")
    public ResponseEntity<List<DeviceDTO>> getMyAssignedDevices(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") Long myUserId) {

        if (!"CLIENT".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Requires CLIENT role.");
        }

        log.info("CLIENT request to get devices for user: {}", myUserId);

        List<DeviceDTO> deviceDTOs = userDeviceService.getDevicesByUserId(myUserId);

        return ResponseEntity.ok(deviceDTOs);
    }
}