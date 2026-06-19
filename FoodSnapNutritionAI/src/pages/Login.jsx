import { useState, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Activity } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../context/AuthContext';
import PageTransition from '../animations/PageTransition';
import { getRandomQuote } from '../services/quotes';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, resetPassword } = useAuth();
  const navigate = useNavigate();

  const quote = useMemo(() => getRandomQuote(), []);

  const handleForgotPassword = async () => {
    if (!email) {
      return toast.error('Please enter your email address first to reset your password');
    }
    let toastId;
    try {
      toastId = toast.loading('Sending password reset email...');
      await resetPassword(email);
      toast.dismiss(toastId);
      toast.success('Password reset email sent! Please check your inbox.');
    } catch (error) {
      if (toastId) toast.dismiss(toastId);
      toast.error(error.message || 'Failed to send password reset email');
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      return toast.error('Please enter both email and password');
    }

    let toastId;
    try {
      setLoading(true);
      toastId = toast.loading('Authenticating credentials...');
      await login(email, password);
      toast.dismiss(toastId);
      toast.success('Signed in successfully!');
      navigate('/dashboard');
    } catch (error) {
      if (toastId) {
        toast.dismiss(toastId);
      }
      // (L-2 FIX) Generic error message to prevent user enumeration
      toast.error('Invalid email or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageTransition className="flex-center page-container">
      <div className="glass-card glow-effect" style={{ padding: '40px', width: '100%', maxWidth: '420px', borderRadius: '24px' }}>
        <div className="flex-center" style={{ marginBottom: '24px', flexDirection: 'column' }}>
          <div style={{ padding: '12px', borderRadius: '16px', background: 'rgba(16, 185, 129, 0.1)', marginBottom: '16px' }}>
            <Activity className="text-primary" size={36} />
          </div>
          <h2 className="heading-2" style={{ margin: 0, fontSize: '1.75rem' }}>Welcome Back</h2>
          <p style={{ color: '#6B7280', fontSize: '0.95rem', margin: 0 }}>Login to access your neural nutrition core</p>
        </div>

        {/* Fitness Quote Banner */}
        <div style={{
          padding: '12px 16px',
          borderRadius: '12px',
          background: 'rgba(59, 130, 246, 0.05)',
          borderLeft: '3px solid var(--secondary-blue)',
          marginBottom: '20px',
          textAlign: 'left'
        }}>
          <p style={{ fontStyle: 'italic', fontSize: '0.85rem', color: 'var(--dark-text)', margin: 0, lineHeight: '1.3' }}>
            "{quote.text}"
          </p>
          <p style={{ fontSize: '0.75rem', color: '#6B7280', margin: '4px 0 0 0', textAlign: 'right' }}>— {quote.author}</p>
        </div>

        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '8px', fontSize: '0.9rem', fontWeight: 500, color: 'var(--dark-text)' }}>
              Email Address
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              style={{
                width: '100%',
                padding: '12px 16px',
                borderRadius: '12px',
                border: '1px solid var(--border-color)',
                outline: 'none',
                background: 'var(--background-white)',
                color: 'var(--dark-text)',
                fontSize: '0.95rem',
              }}
            />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '8px', fontSize: '0.9rem', fontWeight: 500, color: 'var(--dark-text)' }}>
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              autoComplete="new-password"
              style={{
                width: '100%',
                padding: '12px 16px',
                borderRadius: '12px',
                border: '1px solid var(--border-color)',
                outline: 'none',
                background: 'var(--background-white)',
                color: 'var(--dark-text)',
                fontSize: '0.95rem',
              }}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={handleForgotPassword}
              style={{ color: 'var(--primary-green)', fontSize: '0.85rem', fontWeight: 500 }}
            >
              Forgot password?
            </button>
          </div>

          <button 
            type="submit" 
            disabled={loading}
            className="btn-primary" 
            style={{ width: '100%', padding: '14px', fontSize: '1.05rem', marginTop: '8px' }}
          >
            {loading ? 'Authenticating...' : 'Sign In Core'}
          </button>
        </form>

        <div style={{ marginTop: '24px', textAlign: 'center', fontSize: '0.9rem', color: '#6B7280' }}>
          Don't have an account?{' '}
          <Link to="/signup" style={{ color: 'var(--primary-green)', fontWeight: 600 }}>
            Sign Up
          </Link>
        </div>
      </div>
    </PageTransition>
  );
};

export default Login;
