package com.example.aries.service;

import com.example.aries.dto.AnalyzeRequest;
import com.example.aries.dto.ArticleResponse;

import java.util.List;

public interface ArticleService {

    ArticleResponse analyzeAndSave(AnalyzeRequest request);

    List<ArticleResponse> getAllArticles();

    void deleteAllArticles();
}
