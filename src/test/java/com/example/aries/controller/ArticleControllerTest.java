package com.example.aries.controller;

import com.example.aries.common.Sentiment;
import com.example.aries.dto.AnalyzeRequest;
import com.example.aries.dto.ArticleResponse;
import com.example.aries.dto.NewsArticleDto;
import com.example.aries.exception.GlobalExceptionHandler;
import com.example.aries.service.ArticleService;
import com.example.aries.service.NewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ArticleControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private NewsService newsService;

    @Mock
    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ArticleController(newsService, articleService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // --- GET /api/news/search ---

    @Test
    void searchNews_returnsBadRequest_whenQueryIsBlank() throws Exception {
        mockMvc.perform(get("/api/news/search").param("q", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Search query cannot be blank"));
    }

    @Test
    void searchNews_returnsArticleList_whenQueryIsValid() throws Exception {
        NewsArticleDto article = new NewsArticleDto();
        article.setTitle("Breaking News");
        when(newsService.search("AI")).thenReturn(List.of(article));

        mockMvc.perform(get("/api/news/search").param("q", "AI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Breaking News"));
    }

    @Test
    void searchNews_returns500_whenServiceThrows() throws Exception {
        when(newsService.search(any())).thenThrow(new RuntimeException("GNews unreachable"));

        mockMvc.perform(get("/api/news/search").param("q", "AI"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    // --- POST /api/articles/analyze ---

    @Test
    void analyzeArticle_returnsBadRequest_whenTitleIsBlank() throws Exception {
        AnalyzeRequest request = new AnalyzeRequest();
        request.setTitle("");
        request.setUrl("https://example.com");

        mockMvc.perform(post("/api/articles/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void analyzeArticle_returnsBadRequest_whenUrlIsBlank() throws Exception {
        AnalyzeRequest request = new AnalyzeRequest();
        request.setTitle("Some Title");
        request.setUrl("");

        mockMvc.perform(post("/api/articles/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void analyzeArticle_returnsAnalyzedArticle_whenRequestIsValid() throws Exception {
        AnalyzeRequest request = new AnalyzeRequest();
        request.setTitle("Test Title");
        request.setUrl("https://example.com");

        ArticleResponse response = ArticleResponse.builder()
                .id("1")
                .title("Test Title")
                .url("https://example.com")
                .summary("A great summary")
                .sentiment(Sentiment.POSITIVE)
                .build();

        when(articleService.analyzeAndSave(any())).thenReturn(response);

        mockMvc.perform(post("/api/articles/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.sentiment").value("POSITIVE"))
                .andExpect(jsonPath("$.summary").value("A great summary"));
    }

    // --- GET /api/articles ---

    @Test
    void getAllArticles_returnsEmptyList_whenNoArticlesAnalyzed() throws Exception {
        when(articleService.getAllArticles()).thenReturn(List.of());

        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- DELETE /api/articles ---

    @Test
    void deleteAllArticles_returns204_andDelegatestoService() throws Exception {
        mockMvc.perform(delete("/api/articles"))
                .andExpect(status().isNoContent());

        verify(articleService).deleteAllArticles();
    }

    @Test
    void getAllArticles_returnsArticleList_whenArticlesExist() throws Exception {
        List<ArticleResponse> articles = List.of(
                ArticleResponse.builder().id("1").title("Article One").sentiment(Sentiment.POSITIVE).build(),
                ArticleResponse.builder().id("2").title("Article Two").sentiment(Sentiment.NEGATIVE).build()
        );
        when(articleService.getAllArticles()).thenReturn(articles);

        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Article One"))
                .andExpect(jsonPath("$[1].sentiment").value("NEGATIVE"));
    }
}
