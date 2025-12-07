package com.energy.support.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String messageId;
    private String userId;
    private String message;
    private LocalDateTime timestamp;
}