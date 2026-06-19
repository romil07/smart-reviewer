package com.example.aries.service.impl;

import com.example.aries.dto.FirecrawlResponse;
import com.example.aries.service.FirecrawlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FirecrawlServiceImpl implements FirecrawlService {

    private final RestClient restClient;
    private final String apiUrl;

    @Autowired
    public FirecrawlServiceImpl(
            @Value("${firecrawl.api.key}") String apiKey,
            @Value("${firecrawl.api.url:https://api.firecrawl.dev}") String apiUrl) {
        this(apiUrl, RestClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build());
    }

    // Package-private for testing
    FirecrawlServiceImpl(String apiUrl, RestClient restClient) {
        this.apiUrl = apiUrl;
        this.restClient = restClient;
    }

    @Override
    public String scrape(String url) {
        log.info("Scraping full article content via Firecrawl: {}", url);
        FirecrawlResponse response = callFirecrawlApi(url);

        if (response == null || !response.isSuccess() || response.getData() == null) {
            log.warn("Firecrawl returned no usable content for: {}", url);
            return null;
        }

        return response.getData().getMarkdown();
    }

    // Package-private for testing
    FirecrawlResponse callFirecrawlApi(String url) {
        return restClient.post()
                .uri(apiUrl + "/v1/scrape")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("url", url, "formats", List.of("markdown")))
                .retrieve()
                .body(FirecrawlResponse.class);
    }
}
