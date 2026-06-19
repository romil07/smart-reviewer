package com.example.aries.controller;

import com.example.aries.dto.AnalyzeRequest;
import com.example.aries.dto.ArticleResponse;
import com.example.aries.dto.NewsArticleDto;
import com.example.aries.service.ArticleService;
import com.example.aries.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ArticleController {

    private final NewsService newsService;
    private final ArticleService articleService;

    public ArticleController(NewsService newsService, ArticleService articleService) {
        this.newsService = newsService;
        this.articleService = articleService;
    }

    @GetMapping("/news/search")
    public ResponseEntity<List<NewsArticleDto>> searchNews(@RequestParam String q) {
        if (q.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be blank");
        }
        return ResponseEntity.ok(newsService.search(q));
    }

    @PostMapping("/articles/analyze")
    public ResponseEntity<ArticleResponse> analyzeArticle(@Valid @RequestBody AnalyzeRequest request) {
        return ResponseEntity.ok(articleService.analyzeAndSave(request));
    }

    @GetMapping("/articles")
    public ResponseEntity<List<ArticleResponse>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    @DeleteMapping("/articles")
    public ResponseEntity<Void> deleteAllArticles() {
        articleService.deleteAllArticles();
        return ResponseEntity.noContent().build();
    }
}
