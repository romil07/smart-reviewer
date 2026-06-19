package com.example.aries.service.impl;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.example.aries.common.Sentiment;
import com.example.aries.service.internal.AnalysisResult;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.anthropic.models.messages.MessageCreateParams;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private AnthropicClient anthropicClient;

    private AnalysisServiceImpl analysisService;

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisServiceImpl(anthropicClient, new ObjectMapper(), "claude-opus-4-8");
    }

    // --- parseAnalysisResult ---

    @Test
    void parseAnalysisResult_parsesValidJson() {
        String json = """
                {"summary": "A good article.", "sentiment": "POSITIVE"}
                """;

        AnalysisResult result = analysisService.parseAnalysisResult(json);

        assertThat(result.getSummary()).isEqualTo("A good article.");
        assertThat(result.getSentiment()).isEqualTo(Sentiment.POSITIVE);
    }

    @Test
    void parseAnalysisResult_stripsJsonCodeFences() {
        String wrapped = """
                ```json
                {"summary": "Neutral coverage.", "sentiment": "NEUTRAL"}
                ```
                """;

        AnalysisResult result = analysisService.parseAnalysisResult(wrapped);

        assertThat(result.getSummary()).isEqualTo("Neutral coverage.");
        assertThat(result.getSentiment()).isEqualTo(Sentiment.NEUTRAL);
    }

    @Test
    void parseAnalysisResult_stripsPlainCodeFences() {
        String wrapped = """
                ```
                {"summary": "Bad news.", "sentiment": "NEGATIVE"}
                ```
                """;

        AnalysisResult result = analysisService.parseAnalysisResult(wrapped);

        assertThat(result.getSummary()).isEqualTo("Bad news.");
        assertThat(result.getSentiment()).isEqualTo(Sentiment.NEGATIVE);
    }

    @Test
    void parseAnalysisResult_throwsRuntimeException_whenJsonIsInvalid() {
        assertThatThrownBy(() -> analysisService.parseAnalysisResult("not valid json"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse LLM model's response");
    }

    // --- buildPrompt ---

    @Test
    void buildPrompt_truncatesContentLongerThan8000Chars() {
        String longContent = "x".repeat(9000);

        String prompt = analysisService.buildPrompt("Title", longContent, "");

        assertThat(prompt).contains("x".repeat(8000) + "...");
        assertThat(prompt).doesNotContain("x".repeat(8001));
    }

    @Test
    void buildPrompt_usesPlaceholder_whenContentIsNull() {
        String prompt = analysisService.buildPrompt("Title", null, "");

        assertThat(prompt).contains("No content available.");
    }

    @Test
    void buildPrompt_includesTitleAndContent() {
        String prompt = analysisService.buildPrompt("My Article", "Short content.", "");

        assertThat(prompt).contains("My Article");
        assertThat(prompt).contains("Short content.");
    }

    // --- analyze() ---

    @Test
    void analyze_throwsRuntimeException_whenClaudeReturnsEmptyContent() {
        Message mockMessage = mock(Message.class);
        when(mockMessage.content()).thenReturn(List.of());
        when(anthropicClient.messages().create(any(MessageCreateParams.class))).thenReturn(mockMessage);

        assertThatThrownBy(() -> analysisService.analyze("Title", "Content", ""))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Empty response from Claude");
    }
}
