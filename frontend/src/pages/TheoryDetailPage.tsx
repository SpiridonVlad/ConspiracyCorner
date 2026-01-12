import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation } from '@apollo/client/react';
import { GET_THEORY, DELETE_THEORY, GET_THEORIES_PAGINATED, VOTE_THEORY } from '../graphql/operations';
import { Theory, TheoryStatus, Comment } from '../types';
import { useAuth } from '../context/AuthContext';
import CommentItem from '../components/CommentItem';
import CommentForm from '../components/CommentForm';
import Loading from '../components/Loading';
import { useState, useEffect } from 'react';

interface TheoryQueryData {
  theory: Theory;
  rootCommentsByTheory: Comment[];
}

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
  
  const [localScore, setLocalScore] = useState(0);
  const [userVote, setUserVote] = useState(0);

  const { data, loading, error } = useQuery<TheoryQueryData>(GET_THEORY, {
    variables: { id },
    skip: !id,
  });

  const [deleteTheory, { loading: deleting }] = useMutation(DELETE_THEORY, {
    refetchQueries: [{ query: GET_THEORIES_PAGINATED }],
    onCompleted: () => navigate('/'),
  });

  const [voteTheory] = useMutation(VOTE_THEORY);

  const theory = data?.theory;
  const isOwner = user && theory?.author?.id === user.id;
  const status = theory && theory.status ? statusConfig[theory.status] : null;

  useEffect(() => {
    if (theory) {
        setLocalScore(theory.score ?? 0);
    }
  }, [theory]);

  const handleDelete = async () => {
    if (!confirm('Are you sure you want to delete this theory? This cannot be undone.')) return;
    await deleteTheory({ variables: { id } });
  };

  const handleVote = async (value: number) => {
    if (!isAuthenticated || !theory) return;

    let scoreChange = 0;
    
    if (userVote === value) {
        setUserVote(0);
        scoreChange = -value;
    } else if (userVote === 0) {
        setUserVote(value);
        scoreChange = value;
    } else {
        setUserVote(value);
        scoreChange = -userVote + value;
    }

    const newScore = localScore + scoreChange;
    setLocalScore(newScore);

    try {
      await voteTheory({ variables: { id: theory.id, value } });
    } catch (err) {
      setLocalScore(localScore);
      setUserVote(userVote);
      console.error("Vote failed", err);
    }
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

  const authorRep = theory.author?.reputation || 0;
  const evidenceUrls = theory.evidenceUrls || [];
  const comments = data?.rootCommentsByTheory || [];

  return (
    <div className="max-w-4xl mx-auto animate-fade-in pb-12">
      <div className="mb-6">
        <Link
          to="/"
          className="text-gray-400 hover:text-green-400 transition-colors inline-flex items-center gap-2"
        >
          ← Back to theories
        </Link>
      </div>

      <div className="bg-gray-900 border border-gray-800 rounded-xl p-8 mb-8 relative">
        <div className="flex flex-wrap items-start justify-between gap-4 mb-6">
          <div className="flex items-center gap-3">
            <span
              className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold border ${status!.color}`}
            >
              <span className="text-sm">{status!.icon}</span> {status!.label}
            </span>
            <span className="text-sm text-gray-500">{formattedDate}</span>
          </div>
          
          {isOwner && (
            <button
              onClick={handleDelete}
              disabled={deleting}
              className="text-red-400 hover:text-red-300 text-sm disabled:opacity-50"
            >
              {deleting ? 'Deleting...' : 'Delete Theory'}
            </button>
          )}
        </div>

        <h1 className="text-3xl font-bold text-gray-100 mb-6">{theory.title}</h1>

        <div className="prose prose-invert prose-green max-w-none mb-8 whitespace-pre-wrap">
          {theory.content}
        </div>

        <div className="flex flex-wrap items-center justify-between gap-6 pt-6 border-t border-gray-800">
          <div className="flex items-center gap-6">
            {/* Voting UI */}
            <div className="flex items-center gap-3 bg-gray-800/50 rounded-lg px-3 py-1.5 border border-gray-700/50">
              <button 
                onClick={() => handleVote(1)}
                className={`text-lg hover:text-orange-500 transition-colors ${userVote === 1 ? 'text-orange-500' : 'text-gray-400'}`}
              >
                ▲
              </button>
              <span className={`text-lg font-bold ${localScore > 0 ? 'text-orange-400' : localScore < 0 ? 'text-blue-400' : 'text-gray-300'}`}>
                {localScore}
              </span>
              <button 
                onClick={() => handleVote(-1)}
                className={`text-lg hover:text-blue-500 transition-colors ${userVote === -1 ? 'text-blue-500' : 'text-gray-400'}`}
              >
                ▼
              </button>
            </div>

            <div className="flex items-center gap-2">
              <span className="text-gray-400">
                {theory.isAnonymousPost ? '🎭' : '👤'} {theory.authorName}
              </span>
              {authorRep > 0 && (
                <span className="px-1.5 py-0.5 rounded text-xs bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                  Rep: {authorRep}
                </span>
              )}
            </div>
          </div>

          {evidenceUrls.length > 0 && (
            <div className="flex flex-col items-end gap-1">
              <span className="text-sm text-gray-400">Evidence links:</span>
              <div className="flex flex-col items-end gap-1">
                {evidenceUrls.map((url, index) => (
                  <a
                    key={index}
                    href={url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-purple-400 hover:text-purple-300 text-sm flex items-center gap-1"
                  >
                    📎 Link {index + 1}
                  </a>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
      <div className="mb-8">
        <h2 className="text-xl font-bold text-gray-100 mb-4 flex items-center gap-2">
          Comments <span className="text-gray-500 text-base font-normal">({comments.length})</span>
        </h2>
        {isAuthenticated ? (
          <CommentForm theoryId={theory.id} />
        ) : (
          <div className="bg-gray-800/30 rounded-lg p-6 text-center">
            <p className="text-gray-400">
              <Link to="/login" className="text-green-400 hover:text-green-300">
                Log in
              </Link>{' '}
              to join the discussion
            </p>
          </div>
        )}
      </div>

      <div className="space-y-4">
        {comments.map((comment) => (
          <CommentItem
            key={comment.id}
            comment={comment}
            theoryId={theory.id}
            currentUserId={user?.id}
          />
        ))}
        {comments.length === 0 && (
          <p className="text-gray-500 text-center py-8">No comments yet. Be the first to uncover the truth!</p>
        )}
      </div>
    </div>
  );
}
