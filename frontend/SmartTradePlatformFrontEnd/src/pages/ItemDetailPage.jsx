import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Layout from '../components/Layout';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorAlert from '../components/ErrorAlert';
import SuccessAlert from '../components/SuccessAlert';
import { useAuth } from '../context/AuthContext';
import { itemService } from '../services/itemService';
import { cartService } from '../services/cartService';
import { wishlistService } from '../services/wishlistService';

const ItemDetailPage = () => {
  const { itemId } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    loadItem();
  }, [itemId]);

  const loadItem = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await itemService.getItem(itemId);
      setItem(data);
    } catch (err) {
      setError('Failed to load item. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!isAuthenticated()) {
      navigate('/login?redirect=' + window.location.pathname);
      return;
    }

    try {
      await cartService.addToCart(user.userId, parseInt(itemId), quantity);
      setSuccess('Item added to cart successfully!');
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError('Failed to add item to cart. Please try again.');
      console.error(err);
    }
  };

  const handleAddToWishlist = async () => {
    if (!isAuthenticated()) {
      navigate('/login?redirect=' + window.location.pathname);
      return;
    }

    try {
      await wishlistService.addToWishlist(user.userId, parseInt(itemId));
      setSuccess('Item added to wishlist successfully!');
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError('Failed to add item to wishlist. Please try again.');
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

  if (!item) {
    return (
      <Layout>
        <ErrorAlert message="Item not found." />
        <Link to="/" className="text-blue-600 hover:underline">
          ← Back to Home
        </Link>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="px-4">
        <Link
          to="/"
          className="text-blue-600 hover:underline mb-4 inline-block"
        >
          ← Back to Home
        </Link>

        <ErrorAlert message={error} onClose={() => setError(null)} />
        <SuccessAlert message={success} onClose={() => setSuccess(null)} />

        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="mb-4">
            <h1 className="text-3xl font-bold text-gray-900 mb-2">
              {item.item_name}
            </h1>
            <span
              className={`inline-block px-3 py-1 rounded text-sm ${
                item.available
                  ? 'bg-green-100 text-green-800'
                  : 'bg-red-100 text-red-800'
              }`}
            >
              {item.available ? 'Available' : 'Sold'}
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h2 className="text-xl font-semibold mb-4">Details</h2>
              <div className="space-y-2">
                <p>
                  <span className="font-semibold">Description:</span>{' '}
                  {item.description || 'No description'}
                </p>
                <p>
                  <span className="font-semibold">Category:</span>{' '}
                  {item.category || 'N/A'}
                </p>
                <p>
                  <span className="font-semibold">Condition:</span>{' '}
                  {item.condition || 'N/A'}
                </p>
                <p>
                  <span className="font-semibold">Price:</span>{' '}
                  <span className="text-2xl font-bold text-blue-600">
                    ${item.item_UserPrice || item.item_NewPrice}
                  </span>
                </p>
                {item.item_NewPrice && item.item_UserPrice && (
                  <p className="text-sm text-gray-500">
                    Original Price: ${item.item_NewPrice}
                  </p>
                )}
                {item.labels && item.labels.length > 0 && (
                  <div>
                    <span className="font-semibold">Labels:</span>
                    <div className="flex flex-wrap gap-2 mt-1">
                      {item.labels.map((label, index) => (
                        <span
                          key={index}
                          className="px-2 py-1 bg-gray-100 rounded text-sm"
                        >
                          {label}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
                <p>
                  <span className="font-semibold">For Sale:</span>{' '}
                  {item.forSale ? 'Yes' : 'No'}
                </p>
                <p>
                  <span className="font-semibold">For Trade:</span>{' '}
                  {item.forTrade ? 'Yes' : 'No'}
                </p>
                {item.user && (
                  <p>
                    <span className="font-semibold">Seller:</span>{' '}
                    {item.user.name || item.user.email}
                  </p>
                )}
              </div>
            </div>

            <div>
              <h2 className="text-xl font-semibold mb-4">Actions</h2>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium mb-2">
                    Quantity:
                  </label>
                  <input
                    type="number"
                    min="1"
                    value={quantity}
                    onChange={(e) => setQuantity(parseInt(e.target.value) || 1)}
                    className="border rounded px-3 py-2 w-24"
                  />
                </div>
                <button
                  onClick={handleAddToCart}
                  disabled={!item.available}
                  className="w-full bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
                >
                  Add to Cart
                </button>
                <button
                  onClick={handleAddToWishlist}
                  className="w-full bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"
                >
                  Add to Wishlist
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default ItemDetailPage;
