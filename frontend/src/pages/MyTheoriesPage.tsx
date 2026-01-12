import { useQuery, useMutation } from '@apollo/client/react';
import { Link } from 'react-router-dom';
import { GET_THEORIES_BY_USER, DELETE_THEORY } from '../graphql/operations';
import { Theory } from '../types';
import { useAuth } from '../context/AuthContext';
import TheoryCard from '../components/TheoryCard';
import Loading from '../components/Loading';
import { useState } from 'react';

export default function MyTheoriesPage() {
  const { user, isAuthenticated } = useAuth();
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const { data, loading, error, refetch } = useQuery<{ theoriesByUser: Theory[] }>(
    GET_THEORIES_BY_USER,
    {
      variables: { userId: user?.id },
      skip: !user?.id,
    }
  );

  const [deleteTheory] = useMutation(DELETE_THEORY, {
    onCompleted: () => {
      setDeletingId(null);
      refetch();
    },
    onError: () => {
      setDeletingId(null);
    },
  });

  const handleDelete = async (theoryId: string) => {
    if (!confirm('Are you sure you want to delete this theory? This cannot be undone.')) return;
    setDeletingId(theoryId);
    await deleteTheory({ variables: { id: theoryId } });
  };

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
            <div key={theory.id} className="relative group">
              <TheoryCard theory={theory} />
              <div className="absolute top-4 right-4 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                <Link
                  to={`/edit/${theory.id}`}
                  className="bg-blue-600 hover:bg-blue-500 text-white px-3 py-1 rounded text-sm transition-colors"
                >
                  Edit
                </Link>
                <button
                  onClick={(e) => {
                    e.preventDefault();
                    handleDelete(theory.id);
                  }}
                  disabled={deletingId === theory.id}
                  className="bg-red-600 hover:bg-red-500 disabled:bg-gray-600 text-white px-3 py-1 rounded text-sm transition-colors"
                >
                  {deletingId === theory.id ? 'Deleting...' : 'Delete'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
