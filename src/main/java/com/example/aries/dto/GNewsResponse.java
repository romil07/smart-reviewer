package com.example.aries.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GNewsResponse {

    private Integer totalArticles;
    private List<NewsArticleDto> articles;
}
