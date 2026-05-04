import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import Layout from '../components/Layout';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorAlert from '../components/ErrorAlert';
import { useAuth } from '../context/AuthContext';
import { itemService } from '../services/itemService';
import { tradeService } from '../services/tradeService';
import { userService } from '../services/userService';

const UserProfilePage = () => {
  const navigate = useNavigate();
  const { user: currentUser, isAuthenticated } = useAuth();
  const [user, setUser] = useState(null);
  const [userItems, setUserItems] = useState([]);
  const [tradeOffersMade, setTradeOffersMade] = useState([]);
  const [tradeOffersReceived, setTradeOffersReceived] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('items');

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate('/login?redirect=/profile');
      return;
    }
    loadUserData();
  }, [isAuthenticated, navigate, currentUser]);

  const loadUserData = async () => {
    if (!currentUser?.userId) return;

    try {
      setLoading(true);
      setError(null);

      // Load user details
      const userData = await userService.getUser(currentUser.userId);
      setUser(userData);

      // Load user's items
      const allItems = await itemService.getAllItems();
      const myItems = allItems.filter(
        (item) => item.user?.userId === currentUser.userId
      );
      setUserItems(myItems);

      // TODO: Load trade offers when backend endpoint is available
      // For now, setting empty arrays
      // const tradesMade = await tradeService.getTradeOffersBySender(currentUser.userId);
      // const tradesReceived = await tradeService.getTradeOffersByReceiver(currentUser.userId);
      setTradeOffersMade([]);
      setTradeOffersReceived([]);
    } catch (err) {
      setError('Failed to load user data. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Layout>
        <LoadingSpinner />
      </Layout>
    );
  }

  if (!user) {
    return (
      <Layout>
        <ErrorAlert message="User not found." />
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="px-4">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">User Profile</h1>

        <ErrorAlert message={error} onClose={() => setError(null)} />

        {/* User Details */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Profile Information</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p>
                <span className="font-semibold">Name:</span> {user.name || 'N/A'}
              </p>
              <p>
                <span className="font-semibold">Email:</span> {user.email}
              </p>
              <p>
                <span className="font-semibold">Phone:</span>{' '}
                {user.phone_no || 'N/A'}
              </p>
            </div>
            <div>
              <p>
                <span className="font-semibold">Trust Score:</span>{' '}
                {user.trustScore || 0}
              </p>
              <p>
                <span className="font-semibold">Total Listings:</span>{' '}
                {userItems.length}
              </p>
              <p>
                <span className="font-semibold">Successful Trades:</span>{' '}
                {user.successfulTrades || 0}
              </p>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="border-b">
            <nav className="flex -mb-px">
              <button
                onClick={() => setActiveTab('items')}
                className={`px-6 py-3 font-medium ${
                  activeTab === 'items'
                    ? 'border-b-2 border-blue-600 text-blue-600'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                My Listings ({userItems.length})
              </button>
              <button
                onClick={() => setActiveTab('trades-made')}
                className={`px-6 py-3 font-medium ${
                  activeTab === 'trades-made'
                    ? 'border-b-2 border-blue-600 text-blue-600'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                Trade Offers Made ({tradeOffersMade.length})
              </button>
              <button
                onClick={() => setActiveTab('trades-received')}
                className={`px-6 py-3 font-medium ${
                  activeTab === 'trades-received'
                    ? 'border-b-2 border-blue-600 text-blue-600'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                Trade Offers Received ({tradeOffersReceived.length})
              </button>
            </nav>
          </div>

          <div className="p-6">
            {/* My Items Tab */}
            {activeTab === 'items' && (
              <div>
                {userItems.length === 0 ? (
                  <div className="text-center py-8 text-gray-500">
                    <p>You haven't listed any items yet.</p>
                    <Link
                      to="/items/new"
                      className="mt-4 inline-block bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                    >
                      List Your First Item
                    </Link>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {userItems.map((item) => (
                      <Link
                        key={item.itemId}
                        to={`/items/${item.itemId}`}
                        className="bg-gray-50 rounded-lg shadow hover:shadow-md transition-shadow p-4"
                      >
                        <h3 className="font-semibold text-lg mb-2">
                          {item.item_name}
                        </h3>
                        <p className="text-gray-600 text-sm mb-2 line-clamp-2">
                          {item.description || 'No description'}
                        </p>
                        <div className="flex justify-between items-center">
                          <span className="text-xl font-bold text-blue-600">
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
                        <Link
                          to={`/items/${item.itemId}/edit`}
                          className="mt-2 inline-block text-blue-600 hover:underline text-sm"
                          onClick={(e) => e.stopPropagation()}
                        >
                          Edit
                        </Link>
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* Trade Offers Made Tab */}
            {activeTab === 'trades-made' && (
              <div>
                {tradeOffersMade.length === 0 ? (
                  <div className="text-center py-8 text-gray-500">
                    <p>You haven't made any trade offers yet.</p>
                    <Link
                      to="/trades"
                      className="mt-4 inline-block bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                    >
                      Create Trade Offer
                    </Link>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {/* TODO: Display trade offers when backend endpoint is available */}
                    <p className="text-gray-500">
                      Trade offers will be displayed here once the backend
                      endpoint is implemented.
                    </p>
                  </div>
                )}
              </div>
            )}

            {/* Trade Offers Received Tab */}
            {activeTab === 'trades-received' && (
              <div>
                {tradeOffersReceived.length === 0 ? (
                  <div className="text-center py-8 text-gray-500">
                    <p>You haven't received any trade offers yet.</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {/* TODO: Display trade offers when backend endpoint is available */}
                    <p className="text-gray-500">
                      Trade offers will be displayed here once the backend
                      endpoint is implemented.
                    </p>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default UserProfilePage;

