import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation } from '@apollo/client/react';
import {
  CREATE_THEORY,
  UPDATE_THEORY,
  GET_THEORY,
  GET_THEORIES_PAGINATED,
} from '../graphql/operations';
import { TheoryStatus, Theory } from '../types';
import { useAuth } from '../context/AuthContext';
import Loading from '../components/Loading';

export default function CreateTheoryPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  const isEditing = !!id;

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [status, setStatus] = useState<TheoryStatus>(TheoryStatus.UNVERIFIED);
  const [evidenceUrls, setEvidenceUrls] = useState<string[]>(['']);
  const [anonymousPost, setAnonymousPost] = useState(user?.anonymousMode || false);
  const [error, setError] = useState('');
  const [formInitialized, setFormInitialized] = useState(false);

  // Fetch existing theory for editing
  const { data: theoryData, loading: theoryLoading } = useQuery<{ theory: Theory }>(
    GET_THEORY,
    {
      variables: { id },
      skip: !isEditing,
    }
  );

  // Populate form when editing - this is an intentional sync from external data
  useEffect(() => {
    if (theoryData?.theory && !formInitialized) {
      const theory = theoryData.theory;
      setTitle(theory.title);
      setContent(theory.content);
      setStatus(theory.status);
      setEvidenceUrls(theory.evidenceUrls.length > 0 ? theory.evidenceUrls : ['']);
      setAnonymousPost(theory.isAnonymousPost);
      setFormInitialized(true);
    }
  }, [theoryData, formInitialized]);

  const [createTheory, { loading: creating }] = useMutation<{ createTheory: Theory }>(CREATE_THEORY, {
    refetchQueries: [{ query: GET_THEORIES_PAGINATED }],
    onCompleted: (data) => {
      navigate(`/theory/${data.createTheory.id}`);
    },
  });

  const [updateTheory, { loading: updating }] = useMutation<{ updateTheory: Theory }>(UPDATE_THEORY, {
    refetchQueries: [{ query: GET_THEORIES_PAGINATED }, { query: GET_THEORY, variables: { id } }],
    onCompleted: () => {
      navigate(`/theory/${id}`);
    },
  });

  const handleAddUrl = () => {
    setEvidenceUrls([...evidenceUrls, '']);
  };

  const handleRemoveUrl = (index: number) => {
    setEvidenceUrls(evidenceUrls.filter((_, i) => i !== index));
  };

  const handleUrlChange = (index: number, value: string) => {
    const newUrls = [...evidenceUrls];
    newUrls[index] = value;
    setEvidenceUrls(newUrls);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // Validation
    if (title.length < 5) {
      setError('Title must be at least 5 characters');
      return;
    }
    if (content.length < 20) {
      setError('Content must be at least 20 characters');
      return;
    }

    const filteredUrls = evidenceUrls.filter((url) => url.trim() !== '');

    const input = {
      title,
      content,
      status,
      evidenceUrls: filteredUrls,
      anonymousPost,
    };

    try {
      if (isEditing) {
        await updateTheory({ variables: { id, input } });
      } else {
        await createTheory({ variables: { input } });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save theory');
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-400 text-lg">
          🔒 You must be logged in to share your truth.
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

  if (theoryLoading) return <Loading />;

  // Check ownership for editing
  if (isEditing && theoryData?.theory && theoryData.theory.author?.id !== user?.id) {
    return (
      <div className="text-center py-12">
        <p className="text-red-400 text-lg">
          🚫 You can only edit your own theories.
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

  const isLoading = creating || updating;

  return (
    <div className="max-w-3xl mx-auto animate-fade-in">
      <h1 className="text-3xl font-bold text-green-400 glow-green mb-6">
        {isEditing ? '✏️ Edit Theory' : '🔮 Share Your Truth'}
      </h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Title */}
        <div>
          <label htmlFor="title" className="block text-sm font-medium text-gray-300 mb-2">
            Theory Title *
          </label>
          <input
            id="title"
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="What truth have you uncovered? (min 5 characters)"
            className="w-full bg-gray-900 border border-gray-700 rounded-lg px-4 py-3 text-gray-100 placeholder-gray-500 focus:outline-none focus:border-green-500 transition-colors"
          />
        </div>

        {/* Content */}
        <div>
          <label htmlFor="content" className="block text-sm font-medium text-gray-300 mb-2">
            Theory Content *
          </label>
          <textarea
            id="content"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="Share the details of your theory... (min 20 characters)"
            rows={8}
            className="w-full bg-gray-900 border border-gray-700 rounded-lg px-4 py-3 text-gray-100 placeholder-gray-500 focus:outline-none focus:border-green-500 transition-colors resize-none"
          />
          <p className="text-xs text-gray-500 mt-1">
            {content.length}/20 characters minimum
          </p>
        </div>

        {/* Status */}
        <div>
          <label className="block text-sm font-medium text-gray-300 mb-2">
            Theory Status
          </label>
          <div className="flex flex-wrap gap-3">
            <button
              type="button"
              onClick={() => setStatus(TheoryStatus.UNVERIFIED)}
              className={`px-4 py-2 rounded-lg transition-colors ${
                status === TheoryStatus.UNVERIFIED
                  ? 'bg-yellow-600 text-white'
                  : 'bg-gray-800 text-gray-300 hover:bg-gray-700'
              }`}
            >
              ❓ Unverified
            </button>
            <button
              type="button"
              onClick={() => setStatus(TheoryStatus.CONFIRMED)}
              className={`px-4 py-2 rounded-lg transition-colors ${
                status === TheoryStatus.CONFIRMED
                  ? 'bg-green-600 text-white'
                  : 'bg-gray-800 text-gray-300 hover:bg-gray-700'
              }`}
            >
              ✅ Confirmed
            </button>
            <button
              type="button"
              onClick={() => setStatus(TheoryStatus.DEBUNKED)}
              className={`px-4 py-2 rounded-lg transition-colors ${
                status === TheoryStatus.DEBUNKED
                  ? 'bg-red-600 text-white'
                  : 'bg-gray-800 text-gray-300 hover:bg-gray-700'
              }`}
            >
              ❌ Debunked
            </button>
          </div>
        </div>

        {/* Evidence URLs */}
        <div>
          <label className="block text-sm font-medium text-gray-300 mb-2">
            Evidence Links
          </label>
          <div className="space-y-3">
            {evidenceUrls.map((url, index) => (
              <div key={index} className="flex gap-2">
                <input
                  type="url"
                  value={url}
                  onChange={(e) => handleUrlChange(index, e.target.value)}
                  placeholder="https://example.com/evidence"
                  className="flex-1 bg-gray-900 border border-gray-700 rounded-lg px-4 py-2 text-gray-100 placeholder-gray-500 focus:outline-none focus:border-green-500 transition-colors"
                />
                {evidenceUrls.length > 1 && (
                  <button
                    type="button"
                    onClick={() => handleRemoveUrl(index)}
                    className="px-3 py-2 bg-red-600/20 hover:bg-red-600/40 text-red-400 rounded-lg transition-colors"
                  >
                    ✕
                  </button>
                )}
              </div>
            ))}
            <button
              type="button"
              onClick={handleAddUrl}
              className="text-sm text-green-400 hover:text-green-300 transition-colors"
            >
              + Add another evidence link
            </button>
          </div>
        </div>

        {/* Anonymous Toggle */}
        <div className="flex items-center gap-3 p-4 bg-gray-900 border border-gray-700 rounded-lg">
          <input
            type="checkbox"
            id="anonymous"
            checked={anonymousPost}
            onChange={(e) => setAnonymousPost(e.target.checked)}
            className="w-5 h-5 rounded border-gray-600 bg-gray-800 text-green-500 focus:ring-green-500 focus:ring-offset-gray-800"
          />
          <label htmlFor="anonymous" className="text-gray-300 cursor-pointer">
            🎭 Post anonymously
            <span className="text-sm text-gray-500 ml-2">
              (Your identity will be hidden)
            </span>
          </label>
        </div>

        {/* Error Message */}
        {error && (
          <div className="p-4 bg-red-600/20 border border-red-600/50 rounded-lg">
            <p className="text-red-400">{error}</p>
          </div>
        )}

        {/* Submit Buttons */}
        <div className="flex items-center gap-4">
          <button
            type="submit"
            disabled={isLoading}
            className="bg-green-600 hover:bg-green-500 disabled:bg-gray-600 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg font-semibold transition-colors"
          >
            {isLoading
              ? 'Transmitting...'
              : isEditing
              ? 'Update Theory'
              : 'Publish Theory'}
          </button>
          <Link
            to={isEditing ? `/theory/${id}` : '/'}
            className="text-gray-400 hover:text-gray-300 transition-colors"
          >
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
}
