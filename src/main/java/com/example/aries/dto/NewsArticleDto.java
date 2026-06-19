package com.example.aries.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsArticleDto {

    private String title;
    private String description;
    private String content;
    private String url;
    private String image;
    private String publishedAt;
    private NewsSource source;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NewsSource {
        private String name;
        private String url;
    }
}
