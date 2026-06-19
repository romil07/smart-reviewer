package com.example.aries.model;

import com.example.aries.common.Sentiment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "analyzed_articles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
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
