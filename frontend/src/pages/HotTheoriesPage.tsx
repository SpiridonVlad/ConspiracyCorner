import { useState } from 'react';
import { useQuery } from '@apollo/client/react';
import { GET_HOT_THEORIES } from '../graphql/operations';
import { Theory } from '../types';
import TheoryCard from '../components/TheoryCard';
import Loading from '../components/Loading';

export default function HotTheoriesPage() {
  const [page, setPage] = useState(1);
  const pageSize = 10;

  const { data, loading, error } = useQuery<{ hotTheories: Theory[] }>(GET_HOT_THEORIES, {
    variables: {
      page: { page, size: pageSize },
    },
  });

  return (
    <div className="animate-fade-in">
      {/* Header */}
      <div className="text-center mb-8">
        <h1 className="text-4xl md:text-5xl font-bold text-red-400 mb-4">
          🔥 HOT THEORIES 🔥
        </h1>
        <p className="text-gray-400 text-lg max-w-2xl mx-auto">
          The most discussed conspiracies in the community. These theories are generating
          the most buzz among truth seekers.
        </p>
      </div>

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
      ) : data?.hotTheories.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-400 text-lg">
            🔍 No hot theories yet.
          </p>
          <p className="text-gray-500 mt-2">
            Start a discussion to make a theory hot!
          </p>
        </div>
      ) : (
        <>
          {/* Theory Grid */}
          <div className="grid gap-6 md:grid-cols-2">
            {data?.hotTheories.map((theory, index) => (
              <div key={theory.id} className="relative">
                {/* Hot Rank Badge */}
                <div className="absolute -top-3 -left-3 z-10 w-10 h-10 bg-red-600 rounded-full flex items-center justify-center text-white font-bold shadow-lg">
                  #{index + 1 + (page - 1) * pageSize}
                </div>
                <TheoryCard theory={theory} />
              </div>
            ))}
          </div>

          {/* Pagination */}
          {data && data.hotTheories.length === pageSize && (
            <div className="flex items-center justify-center gap-4 mt-8">
              <button
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                disabled={page === 1}
                className="px-4 py-2 bg-gray-800 text-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-700 transition-colors"
              >
                ← Previous
              </button>
              <span className="text-gray-400">Page {page}</span>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={data.hotTheories.length < pageSize}
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
