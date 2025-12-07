package com.energy.support.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String messageId;
    private String message;
    private String responseType; // "RULE_BASED", "AI_DRIVEN", "ADMIN"
    private LocalDateTime timestamp;
    private String matchedRuleId; // For debugging/analytics

    public ChatResponse(String message, String responseType) {
        this.message = message;
        this.responseType = responseType;
        this.timestamp = LocalDateTime.now();
    }
}
