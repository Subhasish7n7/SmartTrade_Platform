import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorAlert from '../components/ErrorAlert';
import SuccessAlert from '../components/SuccessAlert';
import { useAuth } from '../context/AuthContext';
import { tradeService } from '../services/tradeService';
import { itemService } from '../services/itemService';
import { userService } from '../services/userService';

const TradePage = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  
  const [formData, setFormData] = useState({
    senderId: '',
    receiverId: '',
    senderItemIds: [],
    receiverItemIds: [],
    status: 'PENDING',
  });

  const [items, setItems] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [formLoading, setFormLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const [senderItems, setSenderItems] = useState([]);
  const [receiverItems, setReceiverItems] = useState([]);

  useEffect(() => {
    // Set sender to current user if authenticated
    if (user && isAuthenticated()) {
      setFormData((prev) => ({
        ...prev,
        senderId: user.userId.toString(),
      }));
      loadInitialData();
    }
  }, [user, isAuthenticated]);

  const loadInitialData = async () => {
    try {
      const [itemsData, usersData] = await Promise.all([
        itemService.getAllItems(),
        userService.getAllUsers(),
      ]);
      setItems(itemsData);
      setUsers(usersData);
    } catch (err) {
      setError('Failed to load data. Please try again.');
      console.error(err);
    } finally {
      setFormLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleSenderChange = (itemId, checked) => {
    if (checked) {
      setSenderItems([...senderItems, itemId]);
    } else {
      setSenderItems(senderItems.filter((id) => id !== itemId));
    }
  };

  const handleReceiverChange = (itemId, checked) => {
    if (checked) {
      setReceiverItems([...receiverItems, itemId]);
    } else {
      setReceiverItems(receiverItems.filter((id) => id !== itemId));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!isAuthenticated()) {
      setError('Please login to create a trade offer.');
      navigate('/login?redirect=/trades');
      return;
    }

    if (!formData.senderId || !formData.receiverId) {
      setError('Please select both sender and receiver.');
      return;
    }

    if (senderItems.length === 0 || receiverItems.length === 0) {
      setError('Please select at least one item from both sender and receiver.');
      return;
    }

    try {
      setLoading(true);
      const tradeData = {
        senderId: parseInt(formData.senderId),
        receiverId: parseInt(formData.receiverId),
        senderItemIds: senderItems.map((id) => parseInt(id)),
        receiverItemIds: receiverItems.map((id) => parseInt(id)),
        status: formData.status,
      };

      await tradeService.createTradeOffer(tradeData);
      setSuccess('Trade offer created successfully!');
      
      // Reset form
      setFormData({
        senderId: '',
        receiverId: '',
        senderItemIds: [],
        receiverItemIds: [],
        status: 'PENDING',
      });
      setSenderItems([]);
      setReceiverItems([]);
      
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError('Failed to create trade offer. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const getItemsByUser = (userId) => {
    if (!userId) return [];
    return items.filter((item) => item.user?.userId === parseInt(userId));
  };

  const handleCreateTradeClick = () => {
    if (!isAuthenticated()) {
      navigate('/login?redirect=/trades');
      return;
    }
  };

  if (formLoading) {
    return (
      <Layout>
        <LoadingSpinner />
      </Layout>
    );
  }

  if (!isAuthenticated()) {
    return (
      <Layout>
        <div className="text-center py-12">
          <h2 className="text-2xl font-bold mb-4">Login Required</h2>
          <p className="text-gray-600 mb-6">
            Please login to create trade offers.
          </p>
          <button
            onClick={handleCreateTradeClick}
            className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
          >
            Go to Login
          </button>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="px-4">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">
          Create Trade Offer
        </h1>

        <ErrorAlert message={error} onClose={() => setError(null)} />
        <SuccessAlert message={success} onClose={() => setSuccess(null)} />

        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-lg shadow-md p-6 max-w-4xl"
        >
          <div className="space-y-6">
            {/* User Selection */}
            <div className="grid grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium mb-2">
                  Sender (You) *
                </label>
                <input
                  type="text"
                  value={user?.name || user?.email || `User ID: ${user?.userId}`}
                  disabled
                  className="w-full border rounded px-3 py-2 bg-gray-100"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Receiver *
                </label>
                <select
                  name="receiverId"
                  value={formData.receiverId}
                  onChange={handleChange}
                  required
                  className="w-full border rounded px-3 py-2"
                >
                  <option value="">Select receiver</option>
                  {users.map((user) => (
                    <option key={user.userId} value={user.userId}>
                      {user.name || user.email} (ID: {user.userId})
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Items Selection */}
            <div className="grid grid-cols-2 gap-6">
              {/* Sender Items */}
              <div>
                <label className="block text-sm font-medium mb-2">
                  Sender Items *
                </label>
                <div className="border rounded p-4 max-h-64 overflow-y-auto">
                  {formData.senderId ? (
                    getItemsByUser(formData.senderId).length > 0 ? (
                      getItemsByUser(formData.senderId).map((item) => (
                        <label
                          key={item.itemId}
                          className="flex items-center space-x-2 p-2 hover:bg-gray-50 rounded"
                        >
                          <input
                            type="checkbox"
                            checked={senderItems.includes(item.itemId)}
                            onChange={(e) =>
                              handleSenderChange(item.itemId, e.target.checked)
                            }
                            className="rounded"
                          />
                          <div className="flex-1">
                            <span className="font-medium">{item.item_name}</span>
                            <span className="text-gray-600 text-sm ml-2">
                              ${item.item_UserPrice || item.item_NewPrice}
                            </span>
                          </div>
                        </label>
                      ))
                    ) : (
                      <p className="text-gray-500 text-sm">
                        No items found for this user.
                      </p>
                    )
                  ) : (
                    <p className="text-gray-500 text-sm">
                      Please select a sender first.
                    </p>
                  )}
                </div>
              </div>

              {/* Receiver Items */}
              <div>
                <label className="block text-sm font-medium mb-2">
                  Receiver Items *
                </label>
                <div className="border rounded p-4 max-h-64 overflow-y-auto">
                  {formData.receiverId ? (
                    getItemsByUser(formData.receiverId).length > 0 ? (
                      getItemsByUser(formData.receiverId).map((item) => (
                        <label
                          key={item.itemId}
                          className="flex items-center space-x-2 p-2 hover:bg-gray-50 rounded"
                        >
                          <input
                            type="checkbox"
                            checked={receiverItems.includes(item.itemId)}
                            onChange={(e) =>
                              handleReceiverChange(item.itemId, e.target.checked)
                            }
                            className="rounded"
                          />
                          <div className="flex-1">
                            <span className="font-medium">{item.item_name}</span>
                            <span className="text-gray-600 text-sm ml-2">
                              ${item.item_UserPrice || item.item_NewPrice}
                            </span>
                          </div>
                        </label>
                      ))
                    ) : (
                      <p className="text-gray-500 text-sm">
                        No items found for this user.
                      </p>
                    )
                  ) : (
                    <p className="text-gray-500 text-sm">
                      Please select a receiver first.
                    </p>
                  )}
                </div>
              </div>
            </div>

            {/* Status */}
            <div>
              <label className="block text-sm font-medium mb-2">Status</label>
              <select
                name="status"
                value={formData.status}
                onChange={handleChange}
                className="w-full border rounded px-3 py-2"
              >
                <option value="PENDING">Pending</option>
                <option value="ACCEPTED">Accepted</option>
                <option value="REJECTED">Rejected</option>
                <option value="COMPLETED">Completed</option>
              </select>
            </div>

            {/* Submit Button */}
            <div className="flex gap-4 pt-4">
              <button
                type="submit"
                disabled={loading}
                className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700 disabled:bg-gray-400"
              >
                {loading ? 'Creating...' : 'Create Trade Offer'}
              </button>
            </div>
          </div>
        </form>

        {/* TODO: Add list of existing trade offers when GET endpoint is implemented */}
        <div className="mt-8 bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4">Trade Offers</h2>
          <p className="text-gray-500 text-sm">
            Trade offer list functionality will be added when the backend GET
            endpoint is available.
          </p>
        </div>
      </div>
    </Layout>
  );
};

export default TradePage;

