import { useQuery } from '@apollo/client/react';
import { Link } from 'react-router-dom';
import { GET_THEORIES_BY_USER } from '../graphql/operations';
import { Theory } from '../types';
import { useAuth } from '../context/AuthContext';
import TheoryCard from '../components/TheoryCard';
import Loading from '../components/Loading';

export default function MyTheoriesPage() {
  const { user, isAuthenticated } = useAuth();

  const { data, loading, error } = useQuery<{ theoriesByUser: Theory[] }>(
    GET_THEORIES_BY_USER,
    {
      variables: { userId: user?.id },
      skip: !user?.id,
    }
  );

  if (!isAuthenticated) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-400 text-lg">
          🔒 You must be logged in to view your theories.
        </p>
        <Link
          to="/login"
          className="inline-block mt-4 bg-green-600 hover:bg-green-500 text-white px-6 py-2 rounded-lg transition-colors"
        >
          Login to Continue
        </Link>
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-bold text-green-400 glow-green">
            📜 My Theories
          </h1>
          <p className="text-gray-500 mt-2">
            Manage your classified documents
          </p>
        </div>
        <Link
          to="/create"
          className="inline-flex items-center gap-2 bg-green-600 hover:bg-green-500 text-white px-6 py-3 rounded-lg transition-colors"
        >
          + New Theory
        </Link>
      </div>

      {/* Content */}
      {loading ? (
        <Loading />
      ) : error ? (
        <div className="text-center py-12">
          <p className="text-red-400 text-lg">
            ⚠️ Unable to retrieve your theories.
          </p>
          <p className="text-gray-500 mt-2">{error.message}</p>
        </div>
      ) : data?.theoriesByUser.length === 0 ? (
        <div className="text-center py-12 bg-gray-900 border border-gray-800 rounded-xl">
          <p className="text-gray-400 text-lg mb-4">
            📂 You haven't shared any theories yet.
          </p>
          <p className="text-gray-500 mb-6">
            Be the first to expose the truth!
          </p>
          <Link
            to="/create"
            className="inline-flex items-center gap-2 bg-green-600 hover:bg-green-500 text-white px-6 py-3 rounded-lg transition-colors"
          >
            Share Your First Theory
          </Link>
        </div>
      ) : (
        <div className="grid gap-6 md:grid-cols-2">
          {data?.theoriesByUser.map((theory) => (
            <TheoryCard key={theory.id} theory={theory} />
          ))}
        </div>
      )}
    </div>
  );
}
