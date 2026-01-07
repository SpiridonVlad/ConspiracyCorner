import { useState } from 'react';
import { TheoryStatus, TheoryFilter as TheoryFilterType } from '../types';

interface TheoryFilterProps {
  onFilterChange: (filter: TheoryFilterType) => void;
  currentFilter: TheoryFilterType;
}

export default function TheoryFilter({ onFilterChange, currentFilter }: TheoryFilterProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const [keyword, setKeyword] = useState(currentFilter.keyword || '');

  const handleStatusChange = (status: TheoryStatus | undefined) => {
    onFilterChange({ ...currentFilter, status });
  };

  const handleKeywordSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onFilterChange({ ...currentFilter, keyword: keyword || undefined });
  };

  const handleHotToggle = () => {
    onFilterChange({
      ...currentFilter,
      hotOnly: !currentFilter.hotOnly,
    });
  };

  const clearFilters = () => {
    setKeyword('');
    onFilterChange({});
  };

  const hasActiveFilters = currentFilter.status || currentFilter.keyword || currentFilter.hotOnly;

  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl p-4 mb-6">
      <div className="flex items-center justify-between">
        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className="flex items-center gap-2 text-gray-300 hover:text-green-400 transition-colors"
        >
          <span className="text-lg">🔍</span>
          <span className="font-semibold">Filter Theories</span>
          <svg
            className={`w-4 h-4 transition-transform ${isExpanded ? 'rotate-180' : ''}`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 9l-7 7-7-7"
            />
          </svg>
        </button>
        {hasActiveFilters && (
          <button
            onClick={clearFilters}
            className="text-sm text-red-400 hover:text-red-300 transition-colors"
          >
            Clear Filters
          </button>
        )}
      </div>

      {isExpanded && (
        <div className="mt-4 space-y-4 animate-fade-in">
          {/* Search */}
          <form onSubmit={handleKeywordSubmit} className="flex gap-2">
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="Search theories..."
              className="flex-1 bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-gray-100 placeholder-gray-500 focus:outline-none focus:border-green-500 transition-colors"
            />
            <button
              type="submit"
              className="bg-green-600 hover:bg-green-500 text-white px-4 py-2 rounded-lg transition-colors"
            >
              Search
            </button>
          </form>

          {/* Status Filter */}
          <div>
            <p className="text-sm text-gray-400 mb-2">Status:</p>
            <div className="flex flex-wrap gap-2">
              <button
                onClick={() => handleStatusChange(undefined)}
                className={`px-3 py-1 rounded-full text-sm transition-colors ${
                  !currentFilter.status
                    ? 'bg-green-600 text-white'
                    : 'bg-gray-800 text-gray-300 hover:bg-gray-700'
                }`}
              >
                All
              </button>
              <button
                onClick={() => handleStatusChange(TheoryStatus.UNVERIFIED)}
                className={`px-3 py-1 rounded-full text-sm transition-colors ${
                  currentFilter.status === TheoryStatus.UNVERIFIED
                    ? 'bg-yellow-600 text-white'
                    : 'bg-gray-800 text-gray-300 hover:bg-gray-700'
                }`}
              >
                ❓ Unverified
              </button>
              <button
                onClick={() => handleStatusChange(TheoryStatus.CONFIRMED)}
                className={`px-3 py-1 rounded-full text-sm transition-colors ${
                  currentFilter.status === TheoryStatus.CONFIRMED
                    ? 'bg-green-600 text-white'
                    : 'bg-gray-800 text-gray-300 hover:bg-gray-700'
                }`}
              >
                ✅ Confirmed
              </button>
              <button
                onClick={() => handleStatusChange(TheoryStatus.DEBUNKED)}
                className={`px-3 py-1 rounded-full text-sm transition-colors ${
                  currentFilter.status === TheoryStatus.DEBUNKED
                    ? 'bg-red-600 text-white'
                    : 'bg-gray-800 text-gray-300 hover:bg-gray-700'
                }`}
              >
                ❌ Debunked
              </button>
            </div>
          </div>

          {/* Hot Theories Toggle */}
          <div className="flex items-center gap-3">
            <button
              onClick={handleHotToggle}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-colors ${
                currentFilter.hotOnly
                  ? 'bg-orange-600 text-white'
                  : 'bg-gray-800 text-gray-300 hover:bg-gray-700'
              }`}
            >
              🔥 Hot Theories Only
            </button>
            <span className="text-xs text-gray-500">
              (Most commented)
            </span>
          </div>
        </div>
      )}
    </div>
  );
}
