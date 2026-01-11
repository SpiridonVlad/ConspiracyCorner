import { Link } from 'react-router-dom';
import { Theory, TheoryStatus } from '../types';
import { useMutation } from '@apollo/client/react';
import { VOTE_THEORY } from '../graphql/operations';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';

interface TheoryCardProps {
  theory: Theory;
}

const statusConfig = {
  [TheoryStatus.UNVERIFIED]: {
    label: 'UNVERIFIED',
    color: 'text-yellow-400 border-yellow-400/50 bg-yellow-400/10',
    icon: '❓',
  },
  [TheoryStatus.DEBUNKED]: {
    label: 'DEBUNKED',
    color: 'text-red-400 border-red-400/50 bg-red-400/10',
    icon: '❌',
  },
  [TheoryStatus.CONFIRMED]: {
    label: 'CONFIRMED',
    color: 'text-green-400 border-green-400/50 bg-green-400/10',
    icon: '✅',
  },
};

export default function TheoryCard({ theory }: TheoryCardProps) {
  const { isAuthenticated } = useAuth();
  const [voteTheory] = useMutation(VOTE_THEORY);
  const [localScore, setLocalScore] = useState(theory.score);
  const [userVote, setUserVote] = useState(0);
  
  const handleVote = async (e: React.MouseEvent, value: number) => {
    e.preventDefault(); // Prevent navigation
    if (!isAuthenticated) return;

    let scoreChange = 0;
    
    // Toggle logic - same as detail page
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

  const status = statusConfig[theory.status];
  const formattedDate = new Date(theory.postedAt).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });

  return (
    <Link
      to={`/theory/${theory.id}`}
      className="block bg-gray-900 border border-gray-800 hover:border-green-600/50 rounded-xl p-6 transition-all duration-300 hover:shadow-lg hover:shadow-green-900/20 group animate-fade-in"
    >
      <div className="flex flex-wrap items-start justify-between gap-3 mb-3">
        <span
          className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-bold border ${status.color}`}
        >
          {status.icon} {status.label}
        </span>
        <div className="flex items-center gap-3 text-xs text-gray-500">
          <span className="flex items-center gap-1">
            💬 {theory.commentCount}
          </span>
          <span>{formattedDate}</span>
        </div>
      </div>

      <h3 className="text-xl font-bold text-gray-100 group-hover:text-green-400 transition-colors mb-3">
        {theory.title}
      </h3>

      <p className="text-gray-400 text-sm line-clamp-3 mb-4">
        {theory.content}
      </p>

      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-500">
            {theory.isAnonymousPost ? '🎭' : '👤'} {theory.authorName}
          </span>
          
          {/* Voting UI */}
          <div className="flex items-center gap-2 bg-gray-800 rounded-lg px-2 py-1 border border-gray-700" onClick={(e) => e.preventDefault()}>
            <button 
              onClick={(e) => handleVote(e, 1)}
              className={`hover:text-orange-500 transition-colors ${userVote === 1 ? 'text-orange-500' : 'text-gray-400'}`}
            >
              ▲
            </button>
            <span className={`text-sm font-bold ${localScore > 0 ? 'text-orange-400' : localScore < 0 ? 'text-blue-400' : 'text-gray-400'}`}>
              {localScore}
            </span>
            <button 
              onClick={(e) => handleVote(e, -1)}
              className={`hover:text-blue-500 transition-colors ${userVote === -1 ? 'text-blue-500' : 'text-gray-400'}`}
            >
              ▼
            </button>
          </div>
        </div>

        {theory.evidenceUrls.length > 0 && (
          <span className="text-xs text-purple-400 flex items-center gap-1">
            📎 {theory.evidenceUrls.length} evidence link{theory.evidenceUrls.length > 1 ? 's' : ''}
          </span>
        )}
      </div>
    </Link>
  );
}
