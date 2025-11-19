package com.energy.monitoring.repository;

import com.energy.monitoring.entity.EnergyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnergyConsumptionRepository extends JpaRepository<EnergyConsumption, Long> {

    Optional<EnergyConsumption> findByDeviceIdAndTimestamp(Long deviceId, LocalDateTime timestamp);

    List<EnergyConsumption> findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(
            Long deviceId, LocalDateTime start, LocalDateTime end);
}