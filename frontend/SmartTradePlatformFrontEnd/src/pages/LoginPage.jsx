import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Layout from '../components/Layout';
import ErrorAlert from '../components/ErrorAlert';
import SuccessAlert from '../components/SuccessAlert';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { userService } from '../services/userService';

const LoginPage = () => {
  const [searchParams] = useSearchParams();
  const isRegister = searchParams.get('register') === 'true';
  const navigate = useNavigate();
  const { login, isAuthenticated, temporaryLogin } = useAuth();

  const [formData, setFormData] = useState({
    username: '',
    email: '',
    name: '',
    phone_no: '',
    password: '',
    confirmPassword: '',
  });
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // If already authenticated, redirect to intended page or home
    if (isAuthenticated()) {
      const redirect = searchParams.get('redirect');
      navigate(redirect || '/');
    }
  }, [isAuthenticated, navigate, searchParams]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    setLoading(true);

    if (!formData.username || !formData.password) {
      setError('Username and password are required.');
      setLoading(false);
      return;
    }

    try {
      // TODO: Update this endpoint when backend implements JWT authentication
      // This is a placeholder - backend should have /auth/login endpoint
      const response = await api.post('/auth/login', {
        username: formData.username,
        password: formData.password,
      });

      // Backend should return: { token: "jwt_token", user: { userId, email, name, ... } }
      const { token, user } = response.data;
      
      login(token, user);
      setSuccess('Login successful!');
      
      const redirect = searchParams.get('redirect');
      setTimeout(() => {
        navigate(redirect || '/');
      }, 1000);
    } catch (err) {
      console.error('Login error:', err);
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data ||
        'Login failed. Please check your credentials and try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!formData.username || !formData.password) {
      setError('Username and password are required.');
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    setLoading(true);

    try {
      // TODO: Update this endpoint when backend implements registration
      // This is a placeholder - backend should have /auth/register endpoint
      const response = await api.post('/auth/register', {
        username: formData.username,
        email: formData.email,
        name: formData.name,
        phone_no: formData.phone_no,
        password: formData.password,
      });

      // Backend should return: { token: "jwt_token", user: { userId, email, name, ... } }
      const { token, user } = response.data;
      
      login(token, user);
      setSuccess('Registration successful!');
      
      setTimeout(() => {
        navigate('/');
      }, 1000);
    } catch (err) {
      console.error('Registration error:', err);
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data ||
        'Registration failed. Please try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout>
      <div className="max-w-md mx-auto px-4">
        <div className="bg-white rounded-lg shadow-md p-6 mt-8">
          <h1 className="text-2xl font-bold text-gray-900 mb-6">
            {isRegister ? 'Create Account' : 'Login'}
          </h1>

          <ErrorAlert message={error} onClose={() => setError(null)} />
          <SuccessAlert message={success} onClose={() => setSuccess(null)} />

          <form onSubmit={isRegister ? handleRegister : handleLogin}>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">
                  Username *
                </label>
                <input
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  required
                  className="w-full border rounded px-3 py-2"
                />
              </div>

              {isRegister && (
                <>
                  <div>
                    <label className="block text-sm font-medium mb-1">
                      Email
                    </label>
                    <input
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleChange}
                      className="w-full border rounded px-3 py-2"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-1">Name</label>
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleChange}
                      className="w-full border rounded px-3 py-2"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-1">
                      Phone Number
                    </label>
                    <input
                      type="text"
                      name="phone_no"
                      value={formData.phone_no}
                      onChange={handleChange}
                      className="w-full border rounded px-3 py-2"
                    />
                  </div>
                </>
              )}

              <div>
                <label className="block text-sm font-medium mb-1">
                  Password *
                </label>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  className="w-full border rounded px-3 py-2"
                />
              </div>

              {isRegister && (
                <div>
                  <label className="block text-sm font-medium mb-1">
                    Confirm Password *
                  </label>
                  <input
                    type="password"
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    required
                    className="w-full border rounded px-3 py-2"
                  />
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:bg-gray-400"
              >
                {loading
                  ? 'Please wait...'
                  : isRegister
                  ? 'Register'
                  : 'Login'}
              </button>
            </div>
          </form>

          <div className="mt-4 text-center text-sm">
            {isRegister ? (
              <span>
                Already have an account?{' '}
                <a href="/login" className="text-blue-600 hover:underline">
                  Login
                </a>
              </span>
            ) : (
              <span>
                Don't have an account?{' '}
                <a
                  href="/login?register=true"
                  className="text-blue-600 hover:underline"
                >
                  Register
                </a>
              </span>
            )}
          </div>

          {/* Temporary Login Button for Testing */}
          {!isRegister && (
            <div className="mt-6 pt-6 border-t">
              <p className="text-xs text-gray-500 mb-2 text-center">
                Temporary Access (For Testing)
              </p>
              <button
                onClick={async () => {
                  try {
                    // Try to get first user from backend, or use temp login
                    const users = await userService.getAllUsers();
                    if (users.length > 0) {
                      const firstUser = users[0];
                      const tempToken = 'temp-token-for-testing';
                      login(tempToken, firstUser);
                    } else {
                      temporaryLogin();
                    }
                    const redirect = searchParams.get('redirect');
                    setTimeout(() => {
                      navigate(redirect || '/');
                    }, 100);
                  } catch (err) {
                    // If backend fails, use temp login anyway
                    temporaryLogin();
                    const redirect = searchParams.get('redirect');
                    setTimeout(() => {
                      navigate(redirect || '/');
                    }, 100);
                  }
                }}
                className="w-full bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 text-sm"
              >
                Quick Login (Test Mode)
              </button>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};

export default LoginPage;
