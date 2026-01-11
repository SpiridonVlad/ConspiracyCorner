import { useState } from 'react';
import { useMutation } from '@apollo/client/react';
import { Comment } from '../types';
import { DELETE_COMMENT, UPDATE_COMMENT, GET_THEORY, VOTE_COMMENT } from '../graphql/operations';
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
  const [localScore, setLocalScore] = useState(comment.score);
  const [userVote, setUserVote] = useState(0); // 0 = none, 1 = up, -1 = down

  const [updateComment, { loading: updating }] = useMutation(UPDATE_COMMENT, {
    refetchQueries: [{ query: GET_THEORY, variables: { id: theoryId } }],
  });

  const [deleteComment, { loading: deleting }] = useMutation(DELETE_COMMENT, {
    refetchQueries: [{ query: GET_THEORY, variables: { id: theoryId } }],
  });
  
  const [voteComment] = useMutation(VOTE_COMMENT);

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
  
  const handleVote = async (value: number) => {
    if (!isAuthenticated) return;

    let scoreChange = 0;
    
    // Toggle logic
    if (userVote === value) {
        // Removing vote
        setUserVote(0);
        scoreChange = -value;
    } else if (userVote === 0) {
        // New vote
        setUserVote(value);
        scoreChange = value;
    } else {
        // Switching vote (e.g. from -1 to 1)
        setUserVote(value);
        scoreChange = -userVote + value; // e.g. -(-1) + 1 = 2
    }

    const newScore = localScore + scoreChange;
    setLocalScore(newScore);

    try {
      await voteComment({ variables: { id: comment.id, value } });
    } catch (err) {
      // Revert on error
      setLocalScore(localScore);
      setUserVote(userVote); 
      console.error("Vote failed", err);
    }
  };

  const formattedDate = new Date(comment.postedAt).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  // Calculate author reputation style (simple mock for now based on context, or use optional chaining)
  const authorRep = comment.author?.reputation || 0;

  return (
    <div className="bg-gray-800/50 border border-gray-700 rounded-lg p-4 animate-fade-in relative">
      {/* Vote Side Bar */}
      <div className="absolute left-0 top-0 bottom-0 w-8 flex flex-col items-center pt-4 bg-gray-900/30 rounded-l-lg border-r border-gray-700/30">
        <button onClick={() => handleVote(1)} className={`text-xs hover:text-orange-500 ${userVote === 1 ? 'text-orange-500' : 'text-gray-500'}`}>▲</button>
        <span className={`text-xs font-bold my-1 ${localScore > 0 ? 'text-orange-400' : localScore < 0 ? 'text-blue-400' : 'text-gray-500'}`}>{localScore}</span>
        <button onClick={() => handleVote(-1)} className={`text-xs hover:text-blue-500 ${userVote === -1 ? 'text-blue-500' : 'text-gray-500'}`}>▼</button>
      </div>

      <div className="pl-6">
        <div className="flex items-start justify-between mb-2">
            <div className="flex items-center gap-2">
            <span className="text-sm text-gray-400">
                {comment.isAnonymousPost ? '🎭' : '👤'} {comment.authorName}
            </span>
            {authorRep > 0 && (
                 <span className="px-1.5 py-0.5 rounded text-[10px] bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                    Rep: {authorRep}
                 </span>
            )}
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
                className="w-full bg-gray-900 text-gray-100 border border-gray-700 rounded p-2 focus:ring-1 focus:ring-green-500 focus:border-green-500 outline-none resize-none min-h-[100px]"
            />
            <div className="flex items-center gap-2">
                <button
                onClick={handleUpdate}
                disabled={updating}
                className="px-3 py-1 bg-green-600 text-white text-sm rounded hover:bg-green-700 transition-colors disabled:opacity-50"
                >
                {updating ? 'Saving...' : 'Save'}
                </button>
                <button
                onClick={() => setIsEditing(false)}
                className="px-3 py-1 bg-gray-700 text-gray-300 text-sm rounded hover:bg-gray-600 transition-colors"
                >
                Cancel
                </button>
            </div>
            {error && <p className="text-red-400 text-xs">{error}</p>}
            </div>
        ) : (
            <p className="text-gray-300 text-sm whitespace-pre-wrap leading-relaxed">
            {comment.content}
            </p>
        )}
      </div>
    </div>
  );
}
