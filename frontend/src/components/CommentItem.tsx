import { useState } from 'react';
import { useMutation } from '@apollo/client/react';
import { Comment } from '../types';
import { DELETE_COMMENT, UPDATE_COMMENT, GET_THEORY } from '../graphql/operations';
import { useAuth } from '../context/AuthContext';

interface CommentItemProps {
  comment: Comment;
  theoryId: string;
  currentUserId?: string;
}

export default function CommentItem({ comment, theoryId, currentUserId }: CommentItemProps) {
  const { isAuthenticated } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [editContent, setEditContent] = useState(comment.content);
  const [error, setError] = useState('');

  const [updateComment, { loading: updating }] = useMutation(UPDATE_COMMENT, {
    refetchQueries: [{ query: GET_THEORY, variables: { id: theoryId } }],
  });

  const [deleteComment, { loading: deleting }] = useMutation(DELETE_COMMENT, {
    refetchQueries: [{ query: GET_THEORY, variables: { id: theoryId } }],
  });

  const isOwner = currentUserId && comment.author?.id === currentUserId;

  const handleUpdate = async () => {
    if (editContent.length < 10) {
      setError('Comment must be at least 10 characters');
      return;
    }

    try {
      await updateComment({
        variables: { id: comment.id, content: editContent },
      });
      setIsEditing(false);
      setError('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update comment');
    }
  };

  const handleDelete = async () => {
    if (!confirm('Are you sure you want to delete this comment?')) return;

    try {
      await deleteComment({ variables: { id: comment.id } });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete comment');
    }
  };

  const formattedDate = new Date(comment.postedAt).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div className="bg-gray-800/50 border border-gray-700 rounded-lg p-4 animate-fade-in">
      <div className="flex items-start justify-between mb-2">
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-400">
            {comment.isAnonymousPost ? '🎭' : '👤'} {comment.authorName}
          </span>
          <span className="text-xs text-gray-600">•</span>
          <span className="text-xs text-gray-500">{formattedDate}</span>
          {comment.updatedAt && (
            <span className="text-xs text-gray-600 italic">(edited)</span>
          )}
        </div>
        {isAuthenticated && isOwner && !isEditing && (
          <div className="flex items-center gap-2">
            <button
              onClick={() => setIsEditing(true)}
              className="text-xs text-gray-400 hover:text-green-400 transition-colors"
            >
              Edit
            </button>
            <button
              onClick={handleDelete}
              disabled={deleting}
              className="text-xs text-gray-400 hover:text-red-400 transition-colors disabled:opacity-50"
            >
              {deleting ? '...' : 'Delete'}
            </button>
          </div>
        )}
      </div>

      {isEditing ? (
        <div className="space-y-2">
          <textarea
            value={editContent}
            onChange={(e) => setEditContent(e.target.value)}
            className="w-full bg-gray-900 border border-gray-600 rounded-lg p-3 text-gray-100 focus:outline-none focus:border-green-500 transition-colors resize-none"
            rows={3}
          />
          {error && <p className="text-red-400 text-sm">{error}</p>}
          <div className="flex gap-2">
            <button
              onClick={handleUpdate}
              disabled={updating}
              className="bg-green-600 hover:bg-green-500 text-white px-3 py-1 rounded-lg text-sm transition-colors disabled:opacity-50"
            >
              {updating ? 'Saving...' : 'Save'}
            </button>
            <button
              onClick={() => {
                setIsEditing(false);
                setEditContent(comment.content);
                setError('');
              }}
              className="bg-gray-700 hover:bg-gray-600 text-gray-300 px-3 py-1 rounded-lg text-sm transition-colors"
            >
              Cancel
            </button>
          </div>
        </div>
      ) : (
        <p className="text-gray-300 whitespace-pre-wrap">{comment.content}</p>
      )}
    </div>
  );
}
