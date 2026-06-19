package com.example.aries.service.impl;

import com.example.aries.dto.AnalyzeRequest;
import com.example.aries.dto.ArticleResponse;
import com.example.aries.mapper.ArticleMapper;
import com.example.aries.model.Article;
import com.example.aries.repository.ArticleRepository;
import com.example.aries.service.AnalysisService;
import com.example.aries.service.ArticleService;
import com.example.aries.service.FirecrawlService;
import com.example.aries.service.internal.AnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ArticleServiceImpl implements ArticleService {

    private final AnalysisService analysisService;
    private final FirecrawlService firecrawlService;
    private final ArticleRepository articleRepository;

    public ArticleServiceImpl(AnalysisService analysisService,
                              FirecrawlService firecrawlService,
                              ArticleRepository articleRepository) {
        this.analysisService = analysisService;
        this.firecrawlService = firecrawlService;
        this.articleRepository = articleRepository;
    }

    @Override
    public ArticleResponse analyzeAndSave(AnalyzeRequest request) {
        Optional<Article> existing = articleRepository.findByUrl(request.getUrl());
        if (existing.isPresent()) {
            log.info("Article already analyzed, returning cached result: {}", request.getUrl());
            return ArticleMapper.toResponse(existing.get());
        }

        String content = fetchFullContent(request.getUrl(), request.getContent());
        AnalysisResult result = analysisService.analyze(request.getTitle(), content, request.getUrl());

        Article article = Article.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .url(request.getUrl())
                .sourceName(request.getSourceName())
                .publishedAt(request.getPublishedAt())
                .summary(result.getSummary())
                .sentiment(result.getSentiment())
                .analyzedAt(LocalDateTime.now())
                .build();

        try {
            return ArticleMapper.toResponse(articleRepository.save(article));
        } catch (DuplicateKeyException e) {
            log.warn("Race condition detected for URL: {}. Returning existing record.", request.getUrl());
            return articleRepository.findByUrl(request.getUrl())
                    .map(ArticleMapper::toResponse)
                    .orElseThrow(() -> new RuntimeException("Failed to save or retrieve article for URL: " + request.getUrl()));
        }
    }

    private String fetchFullContent(String url, String fallback) {
        try {
            String scraped = firecrawlService.scrape(url);
            if (scraped != null && !scraped.isBlank()) {
                log.info("Using Firecrawl content for: {}", url);
                return scraped;
            }
        } catch (Exception e) {
            log.warn("Firecrawl scraping failed for {}, falling back to original content", url, e);
        }
        return fallback;
    }

    @Override
    public void deleteAllArticles() {
        log.info("Deleting all analyzed articles");
        articleRepository.deleteAll();
    }

    @Override
    public List<ArticleResponse> getAllArticles() {
        // TODO: add Pageable support to avoid loading the full collection as it grows
        return articleRepository.findAllByOrderByAnalyzedAtDesc()
                .stream()
                .map(ArticleMapper::toResponse)
                .toList();
    }
}
