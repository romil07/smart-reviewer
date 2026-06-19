package com.example.aries.migration;

import com.example.aries.model.Article;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MigrationV001CreateArticleUrlIndex implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    public MigrationV001CreateArticleUrlIndex(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            mongoTemplate.indexOps(Article.class).createIndex(new Index().on("url", Sort.Direction.ASC).unique());
            log.info("Migration V001: unique index on 'url' created successfully");
        } catch (Exception e) {
            log.error("Migration V001: failed to create index on 'url'", e);
            throw new RuntimeException("Migration V001 failed — aborting startup", e);
        }
    }
}
