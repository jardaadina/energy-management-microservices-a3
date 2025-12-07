package com.energy.support.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRule {
    private String id;
    private List<String> keywords; // Keywords to match
    private String pattern; // Regex pattern (optional)
    private String response;
    private int priority; // Higher priority rules are checked first
    private boolean caseSensitive;

    public ChatRule(String id, List<String> keywords, String response, int priority) {
        this.id = id;
        this.keywords = keywords;
        this.response = response;
        this.priority = priority;
        this.caseSensitive = false;
    }

    public ChatRule(String id, String pattern, String response) {
        this.id = id;
        this.pattern = pattern;
        this.response = response;
        this.priority = 1;
        this.caseSensitive = false;
    }
}