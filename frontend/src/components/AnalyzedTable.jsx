const SENTIMENT_CLASS = {
  POSITIVE: 'sentiment-positive',
  NEUTRAL:  'sentiment-neutral',
  NEGATIVE: 'sentiment-negative',
}

export default function AnalyzedTable({ articles, highlightId }) {
  if (articles.length === 0) {
    return (
      <p className="empty-state">
        No articles analyzed yet. Search for articles above and click Analyse.
      </p>
    )
  }

  return (
    <div className="table-wrapper">
      <table className="analyzed-table">
        <tbody>
          {articles.map((article) => (
            <tr key={article.id} className={article.id === highlightId ? 'row-highlight' : ''}>
              <td className="meta-cell">
                <table className="meta-table">
                  <tbody>
                    <tr>
                      <th>Title</th>
                      <td>
                        <a href={article.url} target="_blank" rel="noopener noreferrer" className="table-link">
                          {article.title}
                        </a>
                      </td>
                    </tr>
                    <tr>
                      <th>Source</th>
                      <td>{article.sourceName || '—'}</td>
                    </tr>
                    <tr>
                      <th>Published</th>
                      <td>{article.publishedAt ? new Date(article.publishedAt).toLocaleDateString() : '—'}</td>
                    </tr>
                    <tr>
                      <th>Sentiment</th>
                      <td>
                        <span className={`sentiment-badge ${SENTIMENT_CLASS[article.sentiment] ?? ''}`}>
                          {article.sentiment}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>Analysed</th>
                      <td>{article.analyzedAt ? new Date(article.analyzedAt).toLocaleString() : '—'}</td>
                    </tr>
                  </tbody>
                </table>
              </td>
              <td className="summary-cell">
                {article.summary}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
