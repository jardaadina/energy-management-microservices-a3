package com.energy.monitoring.repository;

import com.energy.monitoring.entity.DeviceReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceReferenceRepository extends JpaRepository<DeviceReference, Long> {
}