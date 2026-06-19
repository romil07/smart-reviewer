package com.example.aries.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnalyzeRequest {

    @NotBlank
    private String title;

    private String description;
    private String content;

    @NotBlank
    private String url;

    private String sourceName;
    private String publishedAt;
}
