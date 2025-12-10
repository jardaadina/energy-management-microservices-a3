package com.energy.loadbalancer.dto;

import java.io.Serializable;
import lombok.*;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeviceMeasurement implements Serializable {
    private String deviceId;
    private String timestamp;
    private double measurementValue;

    @Override
    public String toString() {
        return "DeviceMeasurement{" +
                "deviceId='" + deviceId + '\'' +
                ", timestamp=" + timestamp +
                ", measurementValue=" + measurementValue +
                '}';
    }
}