package com.example.aries.dto;

import com.example.aries.common.Sentiment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ArticleResponse {

    private String id;
    private String title;
    private String description;
    private String url;
    private String sourceName;
    private String publishedAt;
    private String summary;
    private Sentiment sentiment;
    private LocalDateTime analyzedAt;
}
