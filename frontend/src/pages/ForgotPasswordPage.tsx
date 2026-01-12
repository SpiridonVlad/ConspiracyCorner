import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation } from '@apollo/client/react';
import { FORGOT_PASSWORD } from '../graphql/operations';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const [forgotPassword, { loading }] = useMutation(FORGOT_PASSWORD);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess(false);

    try {
      await forgotPassword({ variables: { input: { email } } });
      setSuccess(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to send reset email. Please try again.');
    }
  };

  return (
    <div className="max-w-md mx-auto animate-fade-in">
      <div className="text-center mb-8">
        <div className="text-6xl mb-4">🔑</div>
        <h1 className="text-3xl font-bold text-green-400 glow-green">
          RECOVER ACCESS
        </h1>
        <p className="text-gray-500 mt-2">
          Enter your email to receive a temporary password
        </p>
      </div>

      {success ? (
        <div className="bg-gray-900 border border-green-600/50 rounded-xl p-6 text-center">
          <div className="text-4xl mb-4">✉️</div>
          <h2 className="text-xl font-bold text-green-400 mb-2">Check Your Email</h2>
          <p className="text-gray-400 mb-4">
            A temporary password has been sent to your email address.
            Please use it to login and then change your password.
          </p>
          <Link
            to="/login"
            className="inline-block bg-green-600 hover:bg-green-500 text-white px-6 py-2 rounded-lg transition-colors"
          >
            Go to Login
          </Link>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="bg-gray-900 border border-gray-800 rounded-xl p-6 space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-300 mb-2">
              Email Address
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your registered email"
              required
              className="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-3 text-gray-100 placeholder-gray-500 focus:outline-none focus:border-green-500 transition-colors"
            />
          </div>

          {error && (
            <div className="p-3 bg-red-600/20 border border-red-600/50 rounded-lg">
              <p className="text-red-400 text-sm">{error}</p>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-green-600 hover:bg-green-500 disabled:bg-gray-600 disabled:cursor-not-allowed text-white py-3 rounded-lg font-semibold transition-colors"
          >
            {loading ? 'Sending...' : 'Send Temporary Password'}
          </button>
        </form>
      )}

      <p className="text-center text-gray-500 mt-6">
        Remember your password?{' '}
        <Link to="/login" className="text-green-400 hover:text-green-300 transition-colors">
          Back to Login
        </Link>
      </p>
    </div>
  );
}
