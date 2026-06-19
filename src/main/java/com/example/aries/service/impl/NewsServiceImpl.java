package com.example.aries.service.impl;

import com.example.aries.dto.GNewsResponse;
import com.example.aries.dto.NewsArticleDto;
import com.example.aries.service.NewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NewsServiceImpl implements NewsService {

    private final RestClient restClient;
    private final String apiKey;
    private final String apiUrl;

    @Autowired
    public NewsServiceImpl(
            @Value("${gnews.api.key}") String apiKey,
            @Value("${gnews.api.url:https://gnews.io/api/v4}") String apiUrl) {
        this(apiKey, apiUrl, RestClient.create());
    }

    // Package-private for testing
    NewsServiceImpl(String apiKey, String apiUrl, RestClient restClient) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.restClient = restClient;
    }

    @Override
    @Cacheable(value = "news-search", key = "#query.toLowerCase().trim()")
    public List<NewsArticleDto> search(String query) {
        log.info("Searching GNews for: {}", query);
        GNewsResponse response = restClient.get()
                .uri(apiUrl + "/search?q={q}&token={token}&lang=en&max=10&sortby=publishedAt",
                        Map.of("q", query, "token", apiKey))
                .retrieve()
                .body(GNewsResponse.class);

        return (response != null && response.getArticles() != null)
                ? response.getArticles()
                : List.of();
    }
}
