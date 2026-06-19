package com.example.aries.mapper;

import com.example.aries.dto.ArticleResponse;
import com.example.aries.model.Article;

public class ArticleMapper {

    private ArticleMapper() {}

    public static ArticleResponse toResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .url(article.getUrl())
                .sourceName(article.getSourceName())
                .publishedAt(article.getPublishedAt())
                .summary(article.getSummary())
                .sentiment(article.getSentiment())
                .analyzedAt(article.getAnalyzedAt())
                .build();
    }
}
