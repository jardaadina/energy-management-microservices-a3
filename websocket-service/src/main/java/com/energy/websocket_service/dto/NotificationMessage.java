package com.energy.websocket_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String type; // "ALERT", "INFO", "WARNING", "ERROR"
    private String title;
    private String message;
    private LocalDateTime timestamp;
    private Object data;
}