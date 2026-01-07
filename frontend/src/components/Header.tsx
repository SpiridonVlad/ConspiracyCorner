import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Header() {
  const { user, isAuthenticated, logout, toggleAnonymousMode } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const [secretMenuOpen, setSecretMenuOpen] = useState(false);
  const [secretClicks, setSecretClicks] = useState(0);

  const handleLogoClick = () => {
    setSecretClicks((prev) => prev + 1);
    if (secretClicks >= 4) {
      setSecretMenuOpen(!secretMenuOpen);
      setSecretClicks(0);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/');
    setMenuOpen(false);
  };

  return (
    <header className="bg-gray-900 border-b border-green-900/50 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link
            to="/"
            className="flex items-center space-x-2 group"
            onClick={handleLogoClick}
          >
            <span className="text-2xl">👁️</span>
            <span className="text-xl font-bold text-green-400 glow-green group-hover:text-green-300 transition-colors">
              TRUTH FORUM
            </span>
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center space-x-6">
            <Link
              to="/"
              className="text-gray-300 hover:text-green-400 transition-colors"
            >
              All Theories
            </Link>
            <Link
              to="/hot"
              className="text-gray-300 hover:text-red-400 transition-colors flex items-center gap-1"
            >
              <span>🔥</span> Hot Theories
            </Link>
            {isAuthenticated && (
              <>
                <Link
                  to="/create"
                  className="text-gray-300 hover:text-green-400 transition-colors"
                >
                  + New Theory
                </Link>
                <Link
                  to="/my-theories"
                  className="text-gray-300 hover:text-green-400 transition-colors"
                >
                  My Theories
                </Link>
              </>
            )}
          </nav>

          {/* User Menu */}
          <div className="flex items-center space-x-4">
            {isAuthenticated ? (
              <div className="relative">
                <button
                  onClick={() => setMenuOpen(!menuOpen)}
                  className="flex items-center space-x-2 text-gray-300 hover:text-green-400 transition-colors"
                >
                  <span className="text-sm">
                    {user?.anonymousMode ? '🎭' : '👤'}
                  </span>
                  <span className="hidden sm:inline">
                    {user?.anonymousMode ? 'Anonymous' : user?.username}
                  </span>
                  <svg
                    className={`w-4 h-4 transition-transform ${menuOpen ? 'rotate-180' : ''}`}
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M19 9l-7 7-7-7"
                    />
                  </svg>
                </button>

                {menuOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-gray-800 rounded-lg shadow-lg border border-green-900/50 py-1 animate-fade-in">
                    <button
                      onClick={toggleAnonymousMode}
                      className="w-full text-left px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 hover:text-green-400"
                    >
                      {user?.anonymousMode ? '🎭 Disable Anonymous' : '🎭 Enable Anonymous'}
                    </button>
                    <Link
                      to="/my-theories"
                      className="block px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 hover:text-green-400"
                      onClick={() => setMenuOpen(false)}
                    >
                      📜 My Theories
                    </Link>
                    <hr className="border-gray-700 my-1" />
                    <button
                      onClick={handleLogout}
                      className="w-full text-left px-4 py-2 text-sm text-red-400 hover:bg-gray-700"
                    >
                      🚪 Logout
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-center space-x-3">
                <Link
                  to="/login"
                  className="text-gray-300 hover:text-green-400 transition-colors"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="bg-green-600 hover:bg-green-500 text-white px-4 py-2 rounded-lg transition-colors"
                >
                  Join Us
                </Link>
              </div>
            )}

            {/* Mobile menu button */}
            <button
              className="md:hidden text-gray-300 hover:text-green-400"
              onClick={() => setMenuOpen(!menuOpen)}
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 6h16M4 12h16M4 18h16"
                />
              </svg>
            </button>
          </div>
        </div>

        {/* Secret Hidden Menu (Easter Egg) */}
        {secretMenuOpen && (
          <div className="absolute top-16 left-1/2 transform -translate-x-1/2 bg-gray-900 border border-purple-500 rounded-lg p-4 shadow-xl animate-fade-in">
            <p className="text-purple-400 text-sm text-center glow-green">
              🔮 You found the hidden truth... 🔮
            </p>
            <p className="text-xs text-gray-500 mt-2 text-center">
              The code is: TINFOIL2024
            </p>
          </div>
        )}
      </div>

      {/* Mobile Navigation */}
      {menuOpen && (
        <div className="md:hidden bg-gray-800 border-t border-green-900/30 animate-fade-in">
          <nav className="px-4 py-3 space-y-2">
            <Link
              to="/"
              className="block text-gray-300 hover:text-green-400 py-2"
              onClick={() => setMenuOpen(false)}
            >
              All Theories
            </Link>
            <Link
              to="/hot"
              className="block text-gray-300 hover:text-red-400 py-2"
              onClick={() => setMenuOpen(false)}
            >
              🔥 Hot Theories
            </Link>
            {isAuthenticated && (
              <>
                <Link
                  to="/create"
                  className="block text-gray-300 hover:text-green-400 py-2"
                  onClick={() => setMenuOpen(false)}
                >
                  + New Theory
                </Link>
                <Link
                  to="/my-theories"
                  className="block text-gray-300 hover:text-green-400 py-2"
                  onClick={() => setMenuOpen(false)}
                >
                  My Theories
                </Link>
              </>
            )}
          </nav>
        </div>
      )}
    </header>
  );
}
