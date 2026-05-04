import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Sidebar = ({ isOpen, onClose }) => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  if (!isOpen) return null;

  const handleLogout = () => {
    logout();
    onClose();
    navigate('/');
  };

  return (
    <>
      {/* Overlay */}
      <div
        className="fixed inset-0 bg-black bg-opacity-50 z-40"
        onClick={onClose}
      ></div>

      {/* Sidebar */}
      <div className="fixed left-0 top-0 h-full w-64 bg-white shadow-lg z-50 transform transition-transform">
        <div className="p-4">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-bold text-gray-900">Menu</h2>
            <button
              onClick={onClose}
              className="text-gray-500 hover:text-gray-700"
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
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>

          <nav className="space-y-2">
            {/* Add Item - Available to all users */}
            <Link
              to="/items/new"
              onClick={onClose}
              className="block px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-md"
            >
              ➕ Add Item (For Sale)
            </Link>

            {isAuthenticated() ? (
              <>
                <Link
                  to="/profile"
                  onClick={onClose}
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-md"
                >
                  👤 User Profile
                </Link>
                <Link
                  to="/cart"
                  onClick={onClose}
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-md"
                >
                  🛒 Cart
                </Link>
                <Link
                  to="/wishlist"
                  onClick={onClose}
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-md"
                >
                  ❤️ Wishlist
                </Link>
                <Link
                  to="/payment-methods"
                  onClick={onClose}
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-md"
                >
                  💳 Payment Methods
                </Link>
                <Link
                  to="/trades"
                  onClick={onClose}
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-md"
                >
                  🤝 Create Trade
                </Link>
                <button
                  onClick={handleLogout}
                  className="block w-full text-left px-4 py-2 text-red-600 hover:bg-gray-100 rounded-md"
                >
                  🚪 Logout
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  onClick={onClose}
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-md"
                >
                  🔐 Login
                </Link>
                <Link
                  to="/login?register=true"
                  onClick={onClose}
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-md"
                >
                  📝 Register
                </Link>
              </>
            )}
          </nav>
        </div>
      </div>
    </>
  );
};

export default Sidebar;
