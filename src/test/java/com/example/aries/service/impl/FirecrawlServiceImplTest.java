package com.example.aries.service.impl;

import com.example.aries.dto.FirecrawlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class FirecrawlServiceImplTest {

    private FirecrawlServiceImpl firecrawlService;

    @BeforeEach
    void setUp() {
        firecrawlService = spy(new FirecrawlServiceImpl("https://api.firecrawl.dev", RestClient.create()));
    }

    @Test
    void constructor_createsInstance() {
        assertThat(new FirecrawlServiceImpl("test-key", "https://api.firecrawl.dev")).isNotNull();
    }

    @Test
    void scrape_returnsMarkdown_whenApiSucceeds() {
        FirecrawlResponse.FirecrawlData data = new FirecrawlResponse.FirecrawlData();
        data.setMarkdown("# Article Title\n\nFull article content here.");
        FirecrawlResponse response = new FirecrawlResponse();
        response.setSuccess(true);
        response.setData(data);

        doReturn(response).when(firecrawlService).callFirecrawlApi("https://example.com/article");

        assertThat(firecrawlService.scrape("https://example.com/article"))
                .isEqualTo("# Article Title\n\nFull article content here.");
    }

    @Test
    void scrape_returnsNull_whenApiReturnsNullResponse() {
        doReturn(null).when(firecrawlService).callFirecrawlApi("https://example.com/article");

        assertThat(firecrawlService.scrape("https://example.com/article")).isNull();
    }

    @Test
    void scrape_returnsNull_whenApiReturnsUnsuccessfulResponse() {
        FirecrawlResponse response = new FirecrawlResponse();
        response.setSuccess(false);

        doReturn(response).when(firecrawlService).callFirecrawlApi("https://example.com/article");

        assertThat(firecrawlService.scrape("https://example.com/article")).isNull();
    }

    @Test
    void scrape_returnsNull_whenApiReturnsNullData() {
        FirecrawlResponse response = new FirecrawlResponse();
        response.setSuccess(true);
        response.setData(null);

        doReturn(response).when(firecrawlService).callFirecrawlApi("https://example.com/article");

        assertThat(firecrawlService.scrape("https://example.com/article")).isNull();
    }
}
