import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorAlert from '../components/ErrorAlert';
import SuccessAlert from '../components/SuccessAlert';
import { useAuth } from '../context/AuthContext';
import { wishlistService } from '../services/wishlistService';
import { itemService } from '../services/itemService';

const WishlistPage = () => {
  const { userId } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const [wishlistItems, setWishlistItems] = useState([]);
  const [itemDetails, setItemDetails] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate('/login?redirect=/wishlist');
      return;
    }

    const targetUserId = userId || user?.userId;
    if (targetUserId) {
      loadWishlist(targetUserId);
    } else {
      setError('User not found. Please login again.');
      setLoading(false);
    }
  }, [userId, user, isAuthenticated, navigate]);

  const loadWishlist = async (targetUserId) => {
    try {
      setLoading(true);
      setError(null);
      const wishlistData = await wishlistService.getWishlist(targetUserId);
      setWishlistItems(wishlistData);

      // Load item details for each wishlist item
      const details = {};
      for (const wishlistItem of wishlistData) {
        try {
          const item = await itemService.getItem(wishlistItem.itemId);
          details[wishlistItem.itemId] = item;
        } catch (err) {
          console.error(`Failed to load item ${wishlistItem.itemId}:`, err);
        }
      }
      setItemDetails(details);
    } catch (err) {
      setError('Failed to load wishlist. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleRemove = async (itemId) => {
    const targetUserId = userId || user?.userId;
    try {
      await wishlistService.removeFromWishlist(targetUserId, itemId);
      setWishlistItems(
        wishlistItems.filter((item) => item.itemId !== itemId)
      );
      setSuccess('Item removed from wishlist!');
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError('Failed to remove item from wishlist. Please try again.');
      console.error(err);
    }
  };

  if (loading) {
    return (
      <Layout>
        <LoadingSpinner />
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="px-4">
        <Link
          to="/items"
          className="text-blue-600 hover:underline mb-4 inline-block"
        >
          ← Back to Items
        </Link>

        <h1 className="text-3xl font-bold text-gray-900 mb-6">Wishlist</h1>

        <ErrorAlert message={error} onClose={() => setError(null)} />
        <SuccessAlert message={success} onClose={() => setSuccess(null)} />

        {wishlistItems.length === 0 ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-gray-500 text-lg mb-4">
              Your wishlist is empty.
            </p>
            <Link
              to="/items"
              className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700 inline-block"
            >
              Browse Items
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {wishlistItems.map((wishlistItem) => {
              const item = itemDetails[wishlistItem.itemId];
              return (
                <div
                  key={wishlistItem.id}
                  className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow overflow-hidden"
                >
                  {item ? (
                    <>
                      <Link to={`/items/${item.itemId}`} className="block p-6">
                        <h3 className="text-xl font-semibold mb-2">
                          {item.item_name}
                        </h3>
                        <p className="text-gray-600 text-sm mb-2">
                          {item.description || 'No description'}
                        </p>
                        <div className="flex justify-between items-center mt-4">
                          <span className="text-2xl font-bold text-blue-600">
                            ${item.item_UserPrice || item.item_NewPrice}
                          </span>
                          <span
                            className={`px-2 py-1 rounded text-xs ${
                              item.available
                                ? 'bg-green-100 text-green-800'
                                : 'bg-red-100 text-red-800'
                            }`}
                          >
                            {item.available ? 'Available' : 'Sold'}
                          </span>
                        </div>
                      </Link>
                      <div className="px-6 pb-4">
                        <button
                          onClick={() => handleRemove(wishlistItem.itemId)}
                          className="w-full bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
                        >
                          Remove from Wishlist
                        </button>
                      </div>
                    </>
                  ) : (
                    <div className="p-6">
                      <p className="text-gray-500">Item ID: {wishlistItem.itemId}</p>
                      <button
                        onClick={() => handleRemove(wishlistItem.itemId)}
                        className="mt-4 bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
                      >
                        Remove
                      </button>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default WishlistPage;

