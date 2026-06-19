package com.example.aries.service;

import com.example.aries.dto.NewsArticleDto;

import java.util.List;

public interface NewsService {

    List<NewsArticleDto> search(String query);
}
