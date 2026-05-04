import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Sidebar from './Sidebar';

const Layout = ({ children }) => {
  const navigate = useNavigate();
  const { user, logout, isAuthenticated } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleCartClick = () => {
    if (isAuthenticated()) {
      navigate(`/cart`);
    } else {
      navigate('/login?redirect=/cart');
    }
  };

  const handleWishlistClick = () => {
    if (isAuthenticated()) {
      navigate(`/wishlist`);
    } else {
      navigate('/login?redirect=/wishlist');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 ">
      {/* Navigation Bar */}
      <nav className="bg-white shadow-md sticky top-0 z-30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Left: Hamburger Menu */}
            <button
              onClick={() => setSidebarOpen(true)}
              className="p-2 rounded-md text-gray-700  hover:bg-gray-10"
              aria-label="Menu"
            >
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 6h16M4 12h16M4 18h16"
                />
              </svg>
            </button>

            {/* Center: Logo */}
            <Link to="/" className="text-2xl font-bold text-blue-600">
              Smart Trade
            </Link>

            {/* Right: Cart, Wishlist, Login/Register */}
            <div className="flex items-center space-x-4">
              {/* Cart Icon */}
              <button
                onClick={handleCartClick}
                className="relative p-2 text-gray-700 hover:text-blue-600"
                aria-label="Cart"
              >
                <svg
                  className="w-6 h-6"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
                  />
                </svg>
              </button>

              {/* Wishlist Icon */}
              <button
                onClick={handleWishlistClick}
                className="p-2 text-gray-700  hover:text-red-600"
                aria-label="Wishlist"
              >
                <svg
                  className="w-6 h-6"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                  />
                </svg>
              </button>

              {/* Login/Register or User */}
              {isAuthenticated() ? (
                <div className="flex items-center space-x-2">
                  <span className="text-sm text-gray-700  hidden sm:inline">
                    {user?.name || user?.email}
                  </span>
                  <button
                    onClick={() => {
                      logout();
                      navigate('/');
                    }}
                    className="px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-md"
                  >
                    Logout
                  </button>
                </div>
              ) : (
                <>
                  <Link
                    to="/login"
                    className="px-4 py-2 text-sm text-blue-600 hover:bg-blue-50 rounded-md"
                  >
                    Login
                  </Link>
                  <Link
                    to="/login?register=true"
                    className="px-4 py-2 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700"
                  >
                    Register
                  </Link>
                </>
              )}
            </div>
          </div>
        </div>
      </nav>

      {/* Sidebar */}
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        {children}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t mt-12">
        <div className="max-w-7xl mx-auto py-4 px-4 sm:px-6 lg:px-8">
          <p className="text-center text-sm text-gray-500">
            © 2025 Smart Trade Platform
          </p>
        </div>
      </footer>
    </div>
  );
};

export default Layout;
