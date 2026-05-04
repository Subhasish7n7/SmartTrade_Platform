import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorAlert from '../components/ErrorAlert';
import SuccessAlert from '../components/SuccessAlert';
import { useAuth } from '../context/AuthContext';
import { cartService } from '../services/cartService';
import { itemService } from '../services/itemService';

const CartPage = () => {
  const { userId } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const [cartItems, setCartItems] = useState([]);
  const [itemDetails, setItemDetails] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate('/login?redirect=/cart');
      return;
    }

    const targetUserId = userId || user?.userId;
    if (targetUserId) {
      loadCart(targetUserId);
    } else {
      setError('User not found. Please login again.');
      setLoading(false);
    }
  }, [userId, user, isAuthenticated, navigate]);

  const loadCart = async (targetUserId) => {
    try {
      setLoading(true);
      setError(null);
      const cartData = await cartService.getCart(targetUserId);
      setCartItems(cartData);

      // Load item details for each cart item
      const details = {};
      for (const cartItem of cartData) {
        try {
          const item = await itemService.getItem(cartItem.itemId);
          details[cartItem.itemId] = item;
        } catch (err) {
          console.error(`Failed to load item ${cartItem.itemId}:`, err);
        }
      }
      setItemDetails(details);
    } catch (err) {
      setError('Failed to load cart. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleRemove = async (itemId) => {
    const targetUserId = userId || user?.userId;
    try {
      await cartService.removeFromCart(targetUserId, itemId);
      setCartItems(cartItems.filter((item) => item.itemId !== itemId));
      setSuccess('Item removed from cart!');
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError('Failed to remove item from cart. Please try again.');
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

        <h1 className="text-3xl font-bold text-gray-900 mb-6">Shopping Cart</h1>

        <ErrorAlert message={error} onClose={() => setError(null)} />
        <SuccessAlert message={success} onClose={() => setSuccess(null)} />

        {cartItems.length === 0 ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-gray-500 text-lg mb-4">Your cart is empty.</p>
            <Link
              to="/items"
              className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700 inline-block"
            >
              Browse Items
            </Link>
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-md">
            <div className="divide-y">
              {cartItems.map((cartItem) => {
                const item = itemDetails[cartItem.itemId];
                return (
                  <div
                    key={cartItem.id}
                    className="p-6 flex justify-between items-center"
                  >
                    <div className="flex-1">
                      {item ? (
                        <>
                          <Link
                            to={`/items/${item.itemId}`}
                            className="text-xl font-semibold text-blue-600 hover:underline"
                          >
                            {item.item_name}
                          </Link>
                          <p className="text-gray-600 text-sm mt-1">
                            Quantity: {cartItem.quantity}
                          </p>
                          <p className="text-gray-900 font-bold mt-2">
                            ${(item.item_UserPrice || item.item_NewPrice) * cartItem.quantity}
                          </p>
                        </>
                      ) : (
                        <p className="text-gray-500">Item ID: {cartItem.itemId}</p>
                      )}
                    </div>
                    <button
                      onClick={() => handleRemove(cartItem.itemId)}
                      className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
                    >
                      Remove
                    </button>
                  </div>
                );
              })}
            </div>
            <div className="p-6 border-t bg-gray-50">
              <div className="flex justify-between items-center">
                <span className="text-xl font-semibold">Total Items:</span>
                <span className="text-xl font-bold">
                  {cartItems.reduce((sum, item) => sum + item.quantity, 0)}
                </span>
              </div>
              {/* TODO: Add checkout functionality when payment/trade system is implemented */}
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default CartPage;

