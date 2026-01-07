import { useState } from 'react';
import { useQuery } from '@apollo/client/react';
import { GET_THEORIES_PAGINATED } from '../graphql/operations';
import { TheoryFilter as TheoryFilterType, TheoriesPage } from '../types';
import TheoryCard from '../components/TheoryCard';
import TheoryFilter from '../components/TheoryFilter';
import Loading from '../components/Loading';

export default function HomePage() {
  const [filter, setFilter] = useState<TheoryFilterType>({});
  const [page, setPage] = useState(1);
  const pageSize = 10;

  const { data, loading, error } = useQuery<{ theoriesPaginated: TheoriesPage }>(
    GET_THEORIES_PAGINATED,
    {
      variables: {
        filter: filter.hotOnly ? { ...filter, minCommentCount: 1 } : filter,
        page: { page, size: pageSize },
      },
    }
  );

  const handleFilterChange = (newFilter: TheoryFilterType) => {
    setFilter(newFilter);
    setPage(1); // Reset to first page on filter change
  };

  return (
    <div className="animate-fade-in">
      {/* Hero Section */}
      <div className="text-center mb-8">
        <h1 className="text-4xl md:text-5xl font-bold text-green-400 glow-green mb-4">
          UNCOVER THE TRUTH
        </h1>
        <p className="text-gray-400 text-lg max-w-2xl mx-auto">
          Join the community of truth seekers. Share your theories, examine evidence, 
          and discover what <span className="text-green-400">they</span> don't want you to know.
        </p>
      </div>

      {/* Filter Section */}
      <TheoryFilter onFilterChange={handleFilterChange} currentFilter={filter} />

      {/* Content */}
      {loading ? (
        <Loading />
      ) : error ? (
        <div className="text-center py-12">
          <p className="text-red-400 text-lg">
            ⚠️ Connection intercepted. Unable to retrieve classified data.
          </p>
          <p className="text-gray-500 mt-2">{error.message}</p>
        </div>
      ) : data?.theoriesPaginated.content.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-400 text-lg">
            🔍 No theories found matching your criteria.
          </p>
          <p className="text-gray-500 mt-2">
            Try adjusting your filters or be the first to expose the truth!
          </p>
        </div>
      ) : (
        <>
          {/* Stats Bar */}
          <div className="flex items-center justify-between mb-6 text-sm text-gray-500">
            <span>
              Showing {data?.theoriesPaginated.content.length} of{' '}
              {data?.theoriesPaginated.totalElements} classified documents
            </span>
            <span>
              Page {data?.theoriesPaginated.currentPage} of{' '}
              {data?.theoriesPaginated.totalPages}
            </span>
          </div>

          {/* Theory Grid */}
          <div className="grid gap-6 md:grid-cols-2">
            {data?.theoriesPaginated.content.map((theory) => (
              <TheoryCard key={theory.id} theory={theory} />
            ))}
          </div>

          {/* Pagination */}
          {data && data.theoriesPaginated.totalPages > 1 && (
            <div className="flex items-center justify-center gap-4 mt-8">
              <button
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                disabled={!data.theoriesPaginated.hasPrevious}
                className="px-4 py-2 bg-gray-800 text-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-700 transition-colors"
              >
                ← Previous
              </button>
              <div className="flex items-center gap-2">
                {Array.from({ length: Math.min(5, data.theoriesPaginated.totalPages) }, (_, i) => {
                  const pageNum = i + 1;
                  return (
                    <button
                      key={pageNum}
                      onClick={() => setPage(pageNum)}
                      className={`w-10 h-10 rounded-lg transition-colors ${
                        page === pageNum
                          ? 'bg-green-600 text-white'
                          : 'bg-gray-800 text-gray-300 hover:bg-gray-700'
                      }`}
                    >
                      {pageNum}
                    </button>
                  );
                })}
                {data.theoriesPaginated.totalPages > 5 && (
                  <span className="text-gray-500">...</span>
                )}
              </div>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={!data.theoriesPaginated.hasNext}
                className="px-4 py-2 bg-gray-800 text-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-700 transition-colors"
              >
                Next →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
