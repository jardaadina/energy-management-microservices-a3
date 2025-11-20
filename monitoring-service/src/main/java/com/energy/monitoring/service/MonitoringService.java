package com.energy.monitoring.service;

import com.energy.monitoring.dto.DeviceMeasurement;
import com.energy.monitoring.dto.HourlyConsumptionDTO;
import com.energy.monitoring.entity.EnergyConsumption;
import com.energy.monitoring.repository.DeviceReferenceRepository;
import com.energy.monitoring.repository.EnergyConsumptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final EnergyConsumptionRepository consumptionRepository;
    private final DeviceReferenceRepository  referenceRepository;

    @Transactional
    public void processDeviceMeasurement(DeviceMeasurement measurement) {
        LocalDateTime hourlyTimestamp = measurement.getTimestamp()
                .truncatedTo(ChronoUnit.HOURS);

        log.info("Processing measurement for device {} at hour {}",
                measurement.getDeviceId(), hourlyTimestamp);

        EnergyConsumption consumption = consumptionRepository
                .findByDeviceIdAndTimestamp(measurement.getDeviceId(), hourlyTimestamp)
                .orElse(EnergyConsumption.builder()
                        .deviceId(measurement.getDeviceId())
                        .timestamp(hourlyTimestamp)
                        .totalConsumption(0.0)
                        .build());

        consumption.setTotalConsumption(
                consumption.getTotalConsumption() + measurement.getMeasurementValue()
        );

        consumptionRepository.save(consumption);
        log.info("Updated hourly consumption for device {} at {}: {} kWh",
                measurement.getDeviceId(), hourlyTimestamp, consumption.getTotalConsumption());
    }

    @Transactional(readOnly = true)
    public List<HourlyConsumptionDTO> getHourlyConsumptionByDeviceAndDate(Long deviceId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        log.info("Fetching hourly consumption for device {} on date {}", deviceId, date);

        List<EnergyConsumption> consumptions = consumptionRepository
                .findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(
                        deviceId, startOfDay, endOfDay);

        return consumptions.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private HourlyConsumptionDTO toDTO(EnergyConsumption consumption) {
        return HourlyConsumptionDTO.builder()
                .id(consumption.getId())
                .deviceId(consumption.getDeviceId())
                .timestamp(consumption.getTimestamp())
                .totalConsumption(consumption.getTotalConsumption())
                .build();
    }

    @Transactional
    public void deleteDeviceCredentials(Long deviceId) {
        log.info("Attempting to delete all data for device ID: {}", deviceId);

        consumptionRepository.deleteByDeviceId(deviceId);
        log.info("Deleted energy consumption records for device ID: {}", deviceId);

        referenceRepository.deleteById(deviceId);
        log.info("Successfully deleted device reference for device ID: {}", deviceId);
    }
}