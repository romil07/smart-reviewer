# Smart Reviewer

A single-page web application that searches for recent news articles, generates AI-powered summaries and sentiment analysis via Claude, and stores results in MongoDB.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 4.1.0 |
| AI | Anthropic Java SDK (`claude-opus-4-8`) |
| News API | GNews.io |
| Scraping | Firecrawl |
| Database | MongoDB |
| Frontend | React 18 + Vite |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- MongoDB running on `localhost:27017`
- Node.js 18+ and npm
- [GNews.io](https://gnews.io) API key (free tier: 100 req/day)
- [Firecrawl](https://firecrawl.dev) API key (used to fetch full article content)
- Anthropic API key

---

## Setup

### 1. Environment Variables

Set these before starting the backend:

```bash
export ANTHROPIC_API_KEY=your_anthropic_api_key
export GNEWS_API_KEY=your_gnews_api_key
export FIRECRAWL_API_KEY=your_firecrawl_api_key
```

Alternatively, create `src/main/resources/application-local.properties` (git-ignored) with:

```properties
gnews.api.key=your_gnews_api_key
firecrawl.api.key=your_firecrawl_api_key
```

Then run the backend with `-Dspring.profiles.active=local`.

### 2. MongoDB

Make sure MongoDB is running:

```bash
# macOS with Homebrew
brew services start mongodb-community
```

### 3. Backend

```bash
mvn spring-boot:run
# or, if using application-local.properties:
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The API starts on `http://localhost:8080`.

### 4. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` in your browser.

---

## API Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/news/search?q={query}` | Search recent news via GNews |
| POST | `/api/articles/analyze` | Analyse article with Claude; saves to MongoDB |
| GET | `/api/articles` | Fetch all analysed articles (newest first) |

### POST `/api/articles/analyze` — request body

```json
{
  "title": "Article title",
  "description": "Short description",
  "content": "Full article text",
  "url": "https://example.com/article",
  "sourceName": "BBC News",
  "publishedAt": "2026-06-14T10:00:00Z"
}
```

---

## How It Works

1. User types a keyword in the search bar → frontend calls `GET /api/news/search`.
2. Backend fetches up to 10 articles from GNews.io and returns them.
3. User clicks **Analyse** on any article → frontend calls `POST /api/articles/analyze`.
4. Backend uses Firecrawl to scrape the full article content from the URL (falls back to the GNews snippet if scraping fails).
5. Full content is sent to Claude (`claude-opus-4-8`), which returns a summary and a sentiment label (`POSITIVE`, `NEUTRAL`, or `NEGATIVE`).
6. Result is saved to MongoDB and added to the Analysed Articles table.
7. Re-analysing an already-stored URL returns the cached result without a new Claude or Firecrawl call.
