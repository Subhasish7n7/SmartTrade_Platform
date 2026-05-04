import { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check for JWT token in localStorage
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    
    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      } catch (e) {
        console.error('Error parsing saved user:', e);
        // Clear invalid data
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = (token, userData) => {
    setToken(token);
    setUser(userData);
    // Only store JWT token and user data, NOT username/password
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
  };

  // Temporary login for testing (until backend auth is ready)
  const temporaryLogin = () => {
    const tempUser = {
      userId: 1,
      email: 'test@example.com',
      name: 'Test User',
      phone_no: '1234567890',
      trustScore: 0,
      totalListings: 0,
      successfulTrades: 0,
    };
    const tempToken = 'temp-token-for-testing';
    login(tempToken, tempUser);
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };
  // from function to a boolean
  // const isAuthenticated = token !== null && user !== null;
  const isAuthenticated = () => {
    return token !== null && user !== null;
  };

  const getToken = () => {
    return token;
  };

  const value = {
    user,
    token,
    login,
    logout,
    isAuthenticated,
    getToken,
    temporaryLogin,
    loading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
