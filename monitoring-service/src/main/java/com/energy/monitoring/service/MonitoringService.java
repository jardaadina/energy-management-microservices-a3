package com.energy.monitoring.service;

import com.energy.monitoring.dto.DeviceMeasurement;
import com.energy.monitoring.dto.HourlyConsumptionDTO;
import com.energy.monitoring.entity.DeviceReference;
import com.energy.monitoring.entity.EnergyConsumption;
import com.energy.monitoring.repository.DeviceReferenceRepository;
import com.energy.monitoring.repository.EnergyConsumptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final EnergyConsumptionRepository consumptionRepository;
    private final DeviceReferenceRepository  referenceRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange.alerts}")
    private String alertExchange;

    @Value("${rabbitmq.routing-key.alert}")
    private String alertRoutingKey;

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

        double newTotal = consumption.getTotalConsumption() + measurement.getMeasurementValue();
        consumption.setTotalConsumption(newTotal);
        consumptionRepository.save(consumption);

        log.info("Updated hourly consumption for device {} at {}: {} kWh",
                measurement.getDeviceId(), hourlyTimestamp, newTotal);

        checkAndSendAlert(measurement.getDeviceId(), newTotal, hourlyTimestamp);
    }

    private void checkAndSendAlert(Long deviceId, double currentConsumption, LocalDateTime timestamp) {
        try {
            DeviceReference deviceRef = referenceRepository.findById(deviceId).orElse(null);

            if (deviceRef != null && currentConsumption > deviceRef.getMaxConsumption()) {
                log.warn("OVERCONSUMPTION DETECTED! Device: {}, Limit: {}, Current: {}",
                        deviceId, deviceRef.getMaxConsumption(), currentConsumption);

                Map<String, Object> alert = new HashMap<>();
                alert.put("deviceId", deviceId);

                if (deviceRef.getUserId() != null) {
                    alert.put("userId", deviceRef.getUserId());
                } else {
                    log.error("Device {} has no user assigned in monitoring DB! Cannot send alert.", deviceId);
                    return; // Nu putem trimite alerta fără destinatar
                }

                alert.put("timestamp", timestamp.toString());
                alert.put("measurementValue", currentConsumption);
                alert.put("limit", deviceRef.getMaxConsumption());
                alert.put("message", "High energy consumption detected!");

                String jsonAlert = objectMapper.writeValueAsString(alert);
                rabbitTemplate.convertAndSend(alertExchange, alertRoutingKey, jsonAlert);

                log.info("Alert sent to RabbitMQ for User {}: {}", deviceRef.getUserId(), jsonAlert);
            }
        } catch (Exception e) {
            log.error("Failed to process alert logic", e);
        }
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