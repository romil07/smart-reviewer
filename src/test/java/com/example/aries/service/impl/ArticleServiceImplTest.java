package com.example.aries.service.impl;

import com.example.aries.common.Sentiment;
import com.example.aries.dto.AnalyzeRequest;
import com.example.aries.dto.ArticleResponse;
import com.example.aries.model.Article;
import com.example.aries.repository.ArticleRepository;
import com.example.aries.service.AnalysisService;
import com.example.aries.service.FirecrawlService;
import com.example.aries.service.internal.AnalysisResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private AnalysisService analysisService;

    @Mock
    private FirecrawlService firecrawlService;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleServiceImpl articleService;

    @Test
    void analyzeAndSave_returnsCachedResult_whenUrlAlreadyAnalyzed() {
        Article existing = Article.builder()
                .id("1")
                .title("Existing Title")
                .url("https://example.com")
                .summary("Existing summary")
                .sentiment(Sentiment.POSITIVE)
                .analyzedAt(LocalDateTime.now())
                .build();

        when(articleRepository.findByUrl("https://example.com")).thenReturn(Optional.of(existing));

        ArticleResponse result = articleService.analyzeAndSave(requestFor("https://example.com"));

        assertThat(result.getUrl()).isEqualTo("https://example.com");
        assertThat(result.getSentiment()).isEqualTo(Sentiment.POSITIVE);
        verify(firecrawlService, never()).scrape(any());
        verify(analysisService, never()).analyze(any(), any(), any());
        verify(articleRepository, never()).save(any());
    }

    @Test
    void analyzeAndSave_usesFirecrawlContent_whenScrapeSucceeds() {
        when(articleRepository.findByUrl("https://example.com")).thenReturn(Optional.empty());
        when(firecrawlService.scrape("https://example.com")).thenReturn("# Full article content");
        when(analysisService.analyze("Test Title", "# Full article content", "https://example.com"))
                .thenReturn(new AnalysisResult("A great summary", Sentiment.POSITIVE));

        Article saved = Article.builder()
                .id("1")
                .title("Test Title")
                .url("https://example.com")
                .summary("A great summary")
                .sentiment(Sentiment.POSITIVE)
                .analyzedAt(LocalDateTime.now())
                .build();
        when(articleRepository.save(any())).thenReturn(saved);

        ArticleResponse result = articleService.analyzeAndSave(requestFor("https://example.com"));

        assertThat(result.getSummary()).isEqualTo("A great summary");
        assertThat(result.getSentiment()).isEqualTo(Sentiment.POSITIVE);
        verify(analysisService).analyze("Test Title", "# Full article content", "https://example.com");
    }

    @Test
    void analyzeAndSave_fallsBackToOriginalContent_whenFirecrawlFails() {
        when(articleRepository.findByUrl("https://example.com")).thenReturn(Optional.empty());
        when(firecrawlService.scrape("https://example.com")).thenThrow(new RuntimeException("Firecrawl error"));
        when(analysisService.analyze("Test Title", "Some truncated content", "https://example.com"))
                .thenReturn(new AnalysisResult("A summary", Sentiment.NEUTRAL));

        Article saved = Article.builder()
                .id("1")
                .title("Test Title")
                .url("https://example.com")
                .summary("A summary")
                .sentiment(Sentiment.NEUTRAL)
                .analyzedAt(LocalDateTime.now())
                .build();
        when(articleRepository.save(any())).thenReturn(saved);

        AnalyzeRequest request = requestFor("https://example.com");
        request.setContent("Some truncated content");
        ArticleResponse result = articleService.analyzeAndSave(request);

        assertThat(result.getSentiment()).isEqualTo(Sentiment.NEUTRAL);
        verify(analysisService).analyze("Test Title", "Some truncated content", "https://example.com");
    }

    @Test
    void analyzeAndSave_fallsBackToOriginalContent_whenFirecrawlReturnsBlank() {
        when(articleRepository.findByUrl("https://example.com")).thenReturn(Optional.empty());
        when(firecrawlService.scrape("https://example.com")).thenReturn("   ");
        when(analysisService.analyze("Test Title", "Some truncated content", "https://example.com"))
                .thenReturn(new AnalysisResult("A summary", Sentiment.NEUTRAL));

        Article saved = Article.builder()
                .id("1").title("Test Title").url("https://example.com")
                .summary("A summary").sentiment(Sentiment.NEUTRAL).analyzedAt(LocalDateTime.now())
                .build();
        when(articleRepository.save(any())).thenReturn(saved);

        AnalyzeRequest request = requestFor("https://example.com");
        request.setContent("Some truncated content");
        articleService.analyzeAndSave(request);

        verify(analysisService).analyze("Test Title", "Some truncated content", "https://example.com");
    }

    @Test
    void analyzeAndSave_returnsExistingRecord_onRaceCondition() {
        Article raceWinner = Article.builder()
                .id("2")
                .url("https://example.com")
                .sentiment(Sentiment.NEUTRAL)
                .build();

        when(articleRepository.findByUrl("https://example.com"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(raceWinner));
        when(firecrawlService.scrape(any())).thenReturn(null);
        when(analysisService.analyze(any(), any(), any()))
                .thenReturn(new AnalysisResult("Summary", Sentiment.NEUTRAL));
        when(articleRepository.save(any())).thenThrow(new DuplicateKeyException("duplicate key"));

        ArticleResponse result = articleService.analyzeAndSave(requestFor("https://example.com"));

        assertThat(result.getSentiment()).isEqualTo(Sentiment.NEUTRAL);
    }

    @Test
    void getAllArticles_returnsMappedArticlesInOrder() {
        List<Article> articles = List.of(
                Article.builder().id("1").title("Latest").url("https://a.com").build(),
                Article.builder().id("2").title("Older").url("https://b.com").build()
        );
        when(articleRepository.findAllByOrderByAnalyzedAtDesc()).thenReturn(articles);

        List<ArticleResponse> result = articleService.getAllArticles();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Latest");
        assertThat(result.get(1).getTitle()).isEqualTo("Older");
    }

    private AnalyzeRequest requestFor(String url) {
        AnalyzeRequest request = new AnalyzeRequest();
        request.setTitle("Test Title");
        request.setUrl(url);
        return request;
    }
}
