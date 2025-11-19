package com.energy.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "device_reference")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceReference {

    @Id
    private Long deviceId;

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false)
    private Double maxConsumption;
}