import { Link } from 'react-router-dom';
import { Theory, TheoryStatus } from '../types';

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
        <span className="text-sm text-gray-500">
          {theory.isAnonymousPost ? '🎭' : '👤'} {theory.authorName}
        </span>
        {theory.evidenceUrls.length > 0 && (
          <span className="text-xs text-purple-400 flex items-center gap-1">
            📎 {theory.evidenceUrls.length} evidence link{theory.evidenceUrls.length > 1 ? 's' : ''}
          </span>
        )}
      </div>
    </Link>
  );
}
