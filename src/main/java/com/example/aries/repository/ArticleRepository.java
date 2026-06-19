package com.example.aries.repository;

import com.example.aries.model.Article;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends MongoRepository<Article, String> {

    Optional<Article> findByUrl(String url);

    List<Article> findAllByOrderByAnalyzedAtDesc();
}
