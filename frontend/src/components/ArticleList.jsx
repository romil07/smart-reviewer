export default function ArticleList({ articles, analyzingUrls, onAnalyze }) {
  return (
    <div className="article-list">
      {articles.map((article) => {
        const isAnalyzing = analyzingUrls.has(article.url)
        return (
          <div key={article.url} className="article-card">
            <div className="article-card-body">
              <a
                href={article.url}
                target="_blank"
                rel="noopener noreferrer"
                className="article-title"
              >
                {article.title}
              </a>
              <p className="article-description">{article.description}</p>
              <div className="article-meta">
                <span className="article-source">{article.source?.name}</span>
                <span className="article-date">
                  {article.publishedAt
                    ? new Date(article.publishedAt).toLocaleDateString()
                    : ''}
                </span>
              </div>
            </div>
            <div className="article-card-action">
              <button
                className="btn btn-analyze"
                onClick={() => onAnalyze(article)}
                disabled={isAnalyzing}
              >
                {isAnalyzing ? <><span className="spinner" /> Analysing…</> : 'Analyse'}
              </button>
            </div>
          </div>
        )
      })}
    </div>
  )
}
