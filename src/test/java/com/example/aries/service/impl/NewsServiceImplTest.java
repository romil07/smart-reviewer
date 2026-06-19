package com.example.aries.service.impl;

import com.example.aries.dto.GNewsResponse;
import com.example.aries.dto.NewsArticleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private RestClient restClient;

    private NewsServiceImpl newsService;

    @BeforeEach
    void setUp() {
        newsService = new NewsServiceImpl("test-api-key", "https://gnews.io/api/v4", restClient);
    }

    @Test
    void constructor_createsInstance() {
        assertThat(new NewsServiceImpl("key", "https://gnews.io/api/v4")).isNotNull();
    }

    @Test
    void search_returnsArticles_whenApiRespondsWithResults() {
        NewsArticleDto article = new NewsArticleDto();
        article.setTitle("AI Breakthrough");
        article.setUrl("https://example.com/ai");

        GNewsResponse response = new GNewsResponse();
        response.setArticles(List.of(article));

        when(restClient.get().uri(anyString(), anyMap()).retrieve().body(GNewsResponse.class))
                .thenReturn(response);

        List<NewsArticleDto> result = newsService.search("AI");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("AI Breakthrough");
        assertThat(result.get(0).getUrl()).isEqualTo("https://example.com/ai");
    }

    @Test
    void search_returnsEmptyList_whenApiReturnsNullResponse() {
        when(restClient.get().uri(anyString(), anyMap()).retrieve().body(GNewsResponse.class))
                .thenReturn(null);

        List<NewsArticleDto> result = newsService.search("AI");

        assertThat(result).isEmpty();
    }

    @Test
    void search_returnsEmptyList_whenApiReturnsResponseWithNullArticles() {
        GNewsResponse response = new GNewsResponse();
        response.setArticles(null);

        when(restClient.get().uri(anyString(), anyMap()).retrieve().body(GNewsResponse.class))
                .thenReturn(response);

        List<NewsArticleDto> result = newsService.search("AI");

        assertThat(result).isEmpty();
    }

    @Test
    void search_returnsEmptyList_whenApiReturnsNoArticles() {
        GNewsResponse response = new GNewsResponse();
        response.setArticles(List.of());

        when(restClient.get().uri(anyString(), anyMap()).retrieve().body(GNewsResponse.class))
                .thenReturn(response);

        List<NewsArticleDto> result = newsService.search("nonexistent topic");

        assertThat(result).isEmpty();
    }
}
