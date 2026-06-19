package com.example.aries.service.impl;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlock;
import com.example.aries.service.AnalysisService;
import com.example.aries.service.internal.AnalysisResult;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final AnthropicClient anthropicClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public AnalysisServiceImpl(AnthropicClient anthropicClient,
                               ObjectMapper objectMapper,
                               @Value("${anthropic.model}") String model) {
        this.anthropicClient = anthropicClient;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    @Override
    public AnalysisResult analyze(String title, String content, String url) {
        log.info("Sending article to Claude for analysis: {}", title);
        String prompt = buildPrompt(title, content, url);

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.of(model))
                .maxTokens(1024L)
                .addUserMessage(prompt)
                .build();

        Message response = anthropicClient.messages().create(params);

        String responseText = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(TextBlock::text)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Empty response from Claude"));

        return parseAnalysisResult(responseText);
    }

    // Package-private for testing
    String buildPrompt(String title, String content, String url) {
        String truncatedContent = (content != null && content.length() > 8000)
                ? content.substring(0, 8000) + "..."
                : (content != null ? content : "No content available.");

        return """
                Analyze the following news article and respond with ONLY a JSON object — no extra text.

                Title: %s

                Content: %s

                JSON format:
                {
                  "summary": "2-3 sentence summary of the article",
                  "sentiment": "POSITIVE or NEUTRAL or NEGATIVE"
                }
                """.formatted(title, truncatedContent);
    }

    // Package-private for testing
    AnalysisResult parseAnalysisResult(String text) {
        String cleaned = text.trim();
        if (cleaned.contains("```")) {
            cleaned = cleaned.replaceAll("(?s)```(?:json)?\\s*(.*?)```", "$1").trim();
        }
        try {
            return objectMapper.readValue(cleaned, AnalysisResult.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM model's response: " + cleaned, e);
        }
    }
}
