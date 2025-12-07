package com.energy.support.service;

import com.energy.support.dto.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent}")
    private String apiUrl;

    @Value("${gemini.enabled:true}")
    private boolean geminiEnabled;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AIService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public Mono<ChatResponse> generateAIResponse(String userMessage) {
        if (!geminiEnabled || apiKey == null || apiKey.isEmpty()) {
            log.warn("Gemini AI is not enabled or API key is not configured");
            return Mono.just(new ChatResponse(
                    "AI support is currently disabled.",
                    "AI_UNAVAILABLE"
            ));
        }

        try {
            String prompt = buildPrompt(userMessage);
            Map<String, Object> requestBody = buildGeminiRequest(prompt);

            String urlWithKey = apiUrl + "?key=" + apiKey;

            return webClient.post()
                    .uri(urlWithKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .map(this::parseGeminiResponse)
                    .doOnSuccess(response -> log.info("AI response generated successfully"))
                    .doOnError(error -> log.error("Error calling Gemini API: {}", error.getMessage()))
                    .onErrorReturn(new ChatResponse(
                            "I'm having trouble processing your request right now. Please try again or contact an administrator.",
                            "AI_ERROR"
                    ));

        } catch (Exception e) {
            log.error("Exception in generateAIResponse: {}", e.getMessage(), e);
            return Mono.just(new ChatResponse(
                    "An error occurred while processing your request.",
                    "AI_ERROR"
            ));
        }
    }

    private String buildPrompt(String userMessage) {
        return "You are a helpful customer support agent for an Energy Management System. " +
                "The system allows users to monitor their device energy consumption, view historical data, " +
                "and receive overconsumption alerts. " +
                "Please provide a concise, helpful response to the following user question:\n\n" +
                userMessage + "\n\n" +
                "Keep your response under 150 words and friendly in tone.";
    }

    private Map<String, Object> buildGeminiRequest(String prompt) {

        Map<String, Object> request = new HashMap<>();

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        content.put("parts", List.of(part));

        request.put("contents", List.of(content));

        // CONFIG NOU
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 1000);

        request.put("generationConfig", generationConfig);

        return request;
    }


    private ChatResponse parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // 1. Check candidates
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.size() == 0) {
                return new ChatResponse("AI returned no candidates.", "AI_PARSE_ERROR");
            }

            JsonNode candidate = candidates.get(0);

            // 2. Two possible formats for content:
            JsonNode content = candidate.path("content");

            // Format A: content.parts[*].text
            if (content.has("parts")) {
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText(null);
                    if (text != null) {
                        return new ChatResponse(text.trim(), "AI_DRIVEN");
                    }
                }
            }

            // Format B: Gemini 2.5 / 3.0 returns content as an ARRAY
            if (content.isArray()) {
                for (JsonNode c : content) {
                    JsonNode parts = c.path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        String text = parts.get(0).path("text").asText(null);
                        if (text != null) {
                            return new ChatResponse(text.trim(), "AI_DRIVEN");
                        }
                    }
                }
            }

            // Nothing worked â†’ log and return
            log.warn("Could not extract text from Gemini response: " + responseBody);
            return new ChatResponse(
                    "I received an unclear response. Please rephrase your question or contact an administrator.",
                    "AI_PARSE_ERROR"
            );

        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            return new ChatResponse(
                    "An error occurred while processing the AI response.",
                    "AI_PARSE_ERROR"
            );
        }
    }


    public boolean isEnabled() {
        return geminiEnabled && apiKey != null && !apiKey.equals("AIzaSyBV-_XTwOh-t5awSz7N99tZ2AyAyphdMeE");
    }
}