export default function SearchBar({ query, setQuery, onSearch, loading }) {
  const handleKeyDown = (e) => {
    if (e.key === 'Enter') onSearch()
  }

  return (
    <div className="search-bar">
      <input
        type="text"
        className="search-input"
        placeholder="Search for news articles..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        onKeyDown={handleKeyDown}
        disabled={loading}
      />
      <button
        className="btn btn-primary"
        onClick={onSearch}
        disabled={loading || !query.trim()}
      >
        {loading ? <span className="spinner" /> : 'Search'}
      </button>
    </div>
  )
}
