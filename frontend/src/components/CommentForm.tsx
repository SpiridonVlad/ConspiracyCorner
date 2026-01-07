import { useState } from 'react';
import { useMutation } from '@apollo/client/react';
import { CREATE_COMMENT, GET_THEORY } from '../graphql/operations';
import { useAuth } from '../context/AuthContext';

interface CommentFormProps {
  theoryId: string;
}

export default function CommentForm({ theoryId }: CommentFormProps) {
  const { isAuthenticated, user } = useAuth();
  const [content, setContent] = useState('');
  const [anonymousPost, setAnonymousPost] = useState(user?.anonymousMode || false);
  const [error, setError] = useState('');

  const [createComment, { loading }] = useMutation(CREATE_COMMENT, {
    refetchQueries: [{ query: GET_THEORY, variables: { id: theoryId } }],
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (content.length < 10) {
      setError('Comment must be at least 10 characters');
      return;
    }

    try {
      await createComment({
        variables: {
          input: {
            content,
            theoryId,
            anonymousPost,
          },
        },
      });
      setContent('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to post comment');
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="bg-gray-800/50 border border-gray-700 rounded-lg p-4 text-center">
        <p className="text-gray-400">
          <a href="/login" className="text-green-400 hover:text-green-300">
            Login
          </a>{' '}
          or{' '}
          <a href="/register" className="text-green-400 hover:text-green-300">
            Register
          </a>{' '}
          to share your truth...
        </p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="bg-gray-800/50 border border-gray-700 rounded-lg p-4">
      <h4 className="text-lg font-semibold text-gray-200 mb-3">
        Share Your Knowledge
      </h4>
      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder="What do you know about this theory? (min 10 characters)"
        className="w-full bg-gray-900 border border-gray-600 rounded-lg p-3 text-gray-100 placeholder-gray-500 focus:outline-none focus:border-green-500 transition-colors resize-none"
        rows={4}
      />
      {error && <p className="text-red-400 text-sm mt-2">{error}</p>}
      
      <div className="flex items-center justify-between mt-3">
        <label className="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={anonymousPost}
            onChange={(e) => setAnonymousPost(e.target.checked)}
            className="w-4 h-4 rounded border-gray-600 bg-gray-800 text-green-500 focus:ring-green-500 focus:ring-offset-gray-800"
          />
          <span className="text-sm text-gray-400">
            🎭 Post anonymously
          </span>
        </label>
        
        <button
          type="submit"
          disabled={loading || content.length < 10}
          className="bg-green-600 hover:bg-green-500 disabled:bg-gray-600 disabled:cursor-not-allowed text-white px-4 py-2 rounded-lg transition-colors"
        >
          {loading ? 'Posting...' : 'Post Comment'}
        </button>
      </div>
    </form>
  );
}
