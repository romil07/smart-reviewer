package com.example.aries.mapper;

import com.example.aries.common.Sentiment;
import com.example.aries.dto.ArticleResponse;
import com.example.aries.model.Article;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        LocalDateTime analyzedAt = LocalDateTime.of(2026, 6, 18, 10, 30);

        Article article = Article.builder()
                .id("abc123")
                .title("Test Title")
                .description("Test description")
                .url("https://example.com/article")
                .sourceName("BBC News")
                .publishedAt("2026-06-18T08:00:00Z")
                .summary("A concise summary")
                .sentiment(Sentiment.POSITIVE)
                .analyzedAt(analyzedAt)
                .build();

        ArticleResponse response = ArticleMapper.toResponse(article);

        assertThat(response.getId()).isEqualTo("abc123");
        assertThat(response.getTitle()).isEqualTo("Test Title");
        assertThat(response.getDescription()).isEqualTo("Test description");
        assertThat(response.getUrl()).isEqualTo("https://example.com/article");
        assertThat(response.getSourceName()).isEqualTo("BBC News");
        assertThat(response.getPublishedAt()).isEqualTo("2026-06-18T08:00:00Z");
        assertThat(response.getSummary()).isEqualTo("A concise summary");
        assertThat(response.getSentiment()).isEqualTo(Sentiment.POSITIVE);
        assertThat(response.getAnalyzedAt()).isEqualTo(analyzedAt);
    }

    @Test
    void toResponse_handlesNullOptionalFields() {
        Article article = Article.builder()
                .id("1")
                .title("Title")
                .url("https://example.com")
                .build();

        ArticleResponse response = ArticleMapper.toResponse(article);

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getDescription()).isNull();
        assertThat(response.getSummary()).isNull();
        assertThat(response.getSentiment()).isNull();
        assertThat(response.getAnalyzedAt()).isNull();
    }
}
