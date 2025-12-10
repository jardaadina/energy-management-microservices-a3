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
    private String responseType;
    private LocalDateTime timestamp;
    private String matchedRuleId;

    public ChatResponse(String message, String responseType) {
        this.message = message;
        this.responseType = responseType;
        this.timestamp = LocalDateTime.now();
    }
}
