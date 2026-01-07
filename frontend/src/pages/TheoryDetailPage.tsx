import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation } from '@apollo/client/react';
import { GET_THEORY, DELETE_THEORY, GET_THEORIES_PAGINATED } from '../graphql/operations';
import { Theory, TheoryStatus } from '../types';
import { useAuth } from '../context/AuthContext';
import CommentItem from '../components/CommentItem';
import CommentForm from '../components/CommentForm';
import Loading from '../components/Loading';

const statusConfig = {
  [TheoryStatus.UNVERIFIED]: {
    label: 'UNVERIFIED',
    color: 'text-yellow-400 border-yellow-400/50 bg-yellow-400/10',
    icon: '❓',
    description: 'This theory has not been verified yet',
  },
  [TheoryStatus.DEBUNKED]: {
    label: 'DEBUNKED',
    color: 'text-red-400 border-red-400/50 bg-red-400/10',
    icon: '❌',
    description: 'This theory has been proven false',
  },
  [TheoryStatus.CONFIRMED]: {
    label: 'CONFIRMED',
    color: 'text-green-400 border-green-400/50 bg-green-400/10',
    icon: '✅',
    description: 'This theory has been confirmed',
  },
};

export default function TheoryDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  const { data, loading, error } = useQuery<{ theory: Theory }>(GET_THEORY, {
    variables: { id },
    skip: !id,
  });

  const [deleteTheory, { loading: deleting }] = useMutation(DELETE_THEORY, {
    refetchQueries: [{ query: GET_THEORIES_PAGINATED }],
    onCompleted: () => navigate('/'),
  });

  const theory = data?.theory;
  const isOwner = user && theory?.author?.id === user.id;
  const status = theory ? statusConfig[theory.status] : null;

  const handleDelete = async () => {
    if (!confirm('Are you sure you want to delete this theory? This cannot be undone.')) return;
    await deleteTheory({ variables: { id } });
  };

  const formattedDate = theory
    ? new Date(theory.postedAt).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    : '';

  if (loading) return <Loading />;

  if (error) {
    return (
      <div className="text-center py-12">
        <p className="text-red-400 text-lg">
          ⚠️ Access denied or theory not found.
        </p>
        <p className="text-gray-500 mt-2">{error.message}</p>
        <Link
          to="/"
          className="inline-block mt-4 text-green-400 hover:text-green-300"
        >
          ← Return to safety
        </Link>
      </div>
    );
  }

  if (!theory) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-400 text-lg">
          🔍 This theory has been classified or doesn't exist.
        </p>
        <Link
          to="/"
          className="inline-block mt-4 text-green-400 hover:text-green-300"
        >
          ← Return to theories
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto animate-fade-in">
      {/* Back Link */}
      <Link
        to="/"
        className="inline-flex items-center gap-2 text-gray-400 hover:text-green-400 transition-colors mb-6"
      >
        ← Back to theories
      </Link>

      {/* Theory Header */}
      <div className="bg-gray-900 border border-gray-800 rounded-xl p-6 mb-6">
        <div className="flex flex-wrap items-start justify-between gap-4 mb-4">
          <span
            className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-sm font-bold border ${status?.color}`}
            title={status?.description}
          >
            {status?.icon} {status?.label}
          </span>
          
          {isAuthenticated && isOwner && (
            <div className="flex items-center gap-2">
              <Link
                to={`/edit/${theory.id}`}
                className="px-3 py-1 bg-gray-700 hover:bg-gray-600 text-gray-300 rounded-lg text-sm transition-colors"
              >
                ✏️ Edit
              </Link>
              <button
                onClick={handleDelete}
                disabled={deleting}
                className="px-3 py-1 bg-red-600/20 hover:bg-red-600/40 text-red-400 rounded-lg text-sm transition-colors disabled:opacity-50"
              >
                {deleting ? 'Deleting...' : '🗑️ Delete'}
              </button>
            </div>
          )}
        </div>

        <h1 className="text-3xl font-bold text-gray-100 mb-4">{theory.title}</h1>

        <div className="flex flex-wrap items-center gap-4 text-sm text-gray-500 mb-6">
          <span>
            {theory.isAnonymousPost ? '🎭' : '👤'} {theory.authorName}
          </span>
          <span>📅 {formattedDate}</span>
          <span>💬 {theory.commentCount} comments</span>
          {theory.updatedAt && (
            <span className="italic">(edited)</span>
          )}
        </div>

        <div className="prose prose-invert max-w-none">
          <p className="text-gray-300 whitespace-pre-wrap leading-relaxed">
            {theory.content}
          </p>
        </div>

        {/* Evidence Links */}
        {theory.evidenceUrls.length > 0 && (
          <div className="mt-6 pt-6 border-t border-gray-800">
            <h3 className="text-lg font-semibold text-purple-400 mb-3">
              📎 Evidence Links
            </h3>
            <ul className="space-y-2">
              {theory.evidenceUrls.map((url, index) => (
                <li key={index}>
                  <a
                    href={url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-blue-400 hover:text-blue-300 underline break-all"
                  >
                    {url}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {/* Comments Section */}
      <div className="bg-gray-900 border border-gray-800 rounded-xl p-6">
        <h2 className="text-xl font-bold text-gray-100 mb-6">
          💬 Discussion ({theory.commentCount})
        </h2>

        {/* Comment Form */}
        <div className="mb-6">
          <CommentForm theoryId={theory.id} />
        </div>

        {/* Comments List */}
        {theory.comments.length === 0 ? (
          <p className="text-gray-500 text-center py-6">
            No comments yet. Be the first to share your knowledge!
          </p>
        ) : (
          <div className="space-y-4">
            {theory.comments.map((comment) => (
              <CommentItem
                key={comment.id}
                comment={comment}
                theoryId={theory.id}
                currentUserId={user?.id}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
