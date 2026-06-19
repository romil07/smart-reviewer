package com.example.aries.dto;

import lombok.Data;

@Data
public class FirecrawlResponse {
    private boolean success;
    private FirecrawlData data;

    @Data
    public static class FirecrawlData {
        private String markdown;
    }
}
