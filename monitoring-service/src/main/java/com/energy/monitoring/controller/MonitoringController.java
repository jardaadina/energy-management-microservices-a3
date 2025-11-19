package com.energy.monitoring.controller;

import com.energy.monitoring.dto.HourlyConsumptionDTO;
import com.energy.monitoring.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
@Slf4j
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping("/device/{deviceId}/consumption")
    public ResponseEntity<List<HourlyConsumptionDTO>> getHourlyConsumption(
            @PathVariable Long deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("REST request to get hourly consumption for device {} on date {}", deviceId, date);

        List<HourlyConsumptionDTO> consumption = monitoringService
                .getHourlyConsumptionByDeviceAndDate(deviceId, date);

        return ResponseEntity.ok(consumption);
    }
}