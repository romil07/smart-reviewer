import { useState, useEffect, useCallback } from 'react'
import SearchBar from './components/SearchBar'
import ArticleList from './components/ArticleList'
import AnalyzedTable from './components/AnalyzedTable'

export default function App() {
  const [query, setQuery] = useState('')
  const [searchResults, setSearchResults] = useState([])
  const [analyzedArticles, setAnalyzedArticles] = useState([])
  const [searching, setSearching] = useState(false)
  const [searchPerformed, setSearchPerformed] = useState(false)
  const [analyzingUrls, setAnalyzingUrls] = useState(new Set())
  const [searchError, setSearchError] = useState(null)
  const [analyzeError, setAnalyzeError] = useState(null)
  const [loadError, setLoadError] = useState(null)
  const [deleting, setDeleting] = useState(false)
  const [lastAnalyzedId, setLastAnalyzedId] = useState(null)

  const fetchAnalyzedArticles = useCallback(async () => {
    setLoadError(null)
    try {
      const res = await fetch('/api/articles')
      if (!res.ok) throw new Error('Failed to load analyzed articles')
      setAnalyzedArticles(await res.json())
    } catch (err) {
      setLoadError('Could not load analyzed articles. Is the server running?')
    }
  }, [])

  useEffect(() => {
    fetchAnalyzedArticles()
  }, [fetchAnalyzedArticles])

  const handleDeleteAll = async () => {
    if (!window.confirm('Delete all analyzed articles? This cannot be undone.')) return
    setDeleting(true)
    try {
      const res = await fetch('/api/articles', { method: 'DELETE' })
      if (!res.ok) throw new Error('Delete failed')
      setAnalyzedArticles([])
    } catch (err) {
      setLoadError('Could not delete articles. Please try again.')
    } finally {
      setDeleting(false)
    }
  }

  const handleSearch = async () => {
    if (!query.trim()) return
    setSearching(true)
    setSearchError(null)
    setSearchResults([])
    setSearchPerformed(false)
    try {
      const res = await fetch(`/api/news/search?q=${encodeURIComponent(query.trim())}`)
      if (!res.ok) throw new Error('Search failed. Please try again.')
      setSearchResults(await res.json())
      setSearchPerformed(true)
    } catch (err) {
      setSearchError(err.message)
    } finally {
      setSearching(false)
    }
  }

  const handleAnalyze = async (article) => {
    setAnalyzingUrls(prev => new Set([...prev, article.url]))
    setAnalyzeError(null)
    try {
      const res = await fetch('/api/articles/analyze', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: article.title,
          description: article.description,
          content: article.content,
          url: article.url,
          sourceName: article.source?.name ?? '',
          publishedAt: article.publishedAt,
        }),
      })
      if (!res.ok) throw new Error('Analysis failed. Please try again.')
      const saved = await res.json()
      setAnalyzedArticles(prev => [saved, ...prev.filter(a => a.url !== saved.url)])
      setLastAnalyzedId(saved.id)
      setTimeout(() => setLastAnalyzedId(null), 2000)
    } catch (err) {
      setAnalyzeError(err.message)
    } finally {
      setAnalyzingUrls(prev => {
        const next = new Set(prev)
        next.delete(article.url)
        return next
      })
    }
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Smart Reviewer</h1>
        <p className="subtitle">News Search, AI Summaries &amp; Sentiment Analysis</p>
      </header>

      <main className="app-main">
        <div className="content-grid">
          <div className="left-panel">
            <section className="search-section">
              <SearchBar
                query={query}
                setQuery={setQuery}
                onSearch={handleSearch}
                loading={searching}
              />
              {searchError && <div className="error-banner">{searchError}</div>}
            </section>

            {(searchPerformed || searchResults.length > 0) && (
              <section className="results-section">
                <h2>Search Results <span className="count">({searchResults.length})</span></h2>
                {searchResults.length === 0
                  ? <p className="empty-state">No articles found for "{query}". Try a different search term.</p>
                  : (
                    <>
                      <ArticleList
                        articles={searchResults}
                        analyzingUrls={analyzingUrls}
                        onAnalyze={handleAnalyze}
                      />
                      {analyzeError && <div className="error-banner">{analyzeError}</div>}
                    </>
                  )
                }
              </section>
            )}
          </div>

          <div className="right-panel">
            <section className="analyzed-section">
              <div className="section-header">
                <h2>Analyzed Articles <span className="count">({analyzedArticles.length})</span></h2>
                <div className="section-header-actions">
                  <button
                    className="btn btn-danger"
                    onClick={handleDeleteAll}
                    disabled={deleting || analyzedArticles.length === 0}
                  >
                    {deleting ? 'Deleting…' : 'Clear All'}
                  </button>
                </div>
              </div>
              {loadError
                ? <div className="error-banner">{loadError}</div>
                : <AnalyzedTable articles={analyzedArticles} highlightId={lastAnalyzedId} />
              }
            </section>
          </div>
        </div>
      </main>
    </div>
  )
}
