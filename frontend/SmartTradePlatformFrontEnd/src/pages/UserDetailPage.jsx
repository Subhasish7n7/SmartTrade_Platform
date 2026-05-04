import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Layout from '../components/Layout';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorAlert from '../components/ErrorAlert';
import { userService } from '../services/userService';

const UserDetailPage = () => {
  const { userId } = useParams();
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadUser();
  }, [userId]);

  const loadUser = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await userService.getUser(userId);
      setUser(data);
    } catch (err) {
      setError('Failed to load user. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this user?')) {
      return;
    }

    try {
      await userService.deleteUser(userId);
      navigate('/users');
    } catch (err) {
      setError('Failed to delete user. Please try again.');
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

  if (!user) {
    return (
      <Layout>
        <ErrorAlert message="User not found." />
        <Link to="/users" className="text-blue-600 hover:underline">
          ← Back to Users
        </Link>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="px-4">
        <Link
          to="/users"
          className="text-blue-600 hover:underline mb-4 inline-block"
        >
          ← Back to Users
        </Link>

        <ErrorAlert message={error} onClose={() => setError(null)} />

        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="flex justify-between items-start mb-4">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 mb-2">
                {user.name || 'No Name'}
              </h1>
              <p className="text-gray-600">{user.email}</p>
            </div>
            <div className="flex gap-2">
              <Link
                to={`/users/${userId}/edit`}
                className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
              >
                Edit
              </Link>
              <button
                onClick={handleDelete}
                className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
              >
                Delete
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h2 className="text-xl font-semibold mb-4">Profile Details</h2>
              <div className="space-y-2">
                <p>
                  <span className="font-semibold">User ID:</span> {user.userId}
                </p>
                <p>
                  <span className="font-semibold">Email:</span> {user.email}
                </p>
                <p>
                  <span className="font-semibold">Name:</span>{' '}
                  {user.name || 'N/A'}
                </p>
                <p>
                  <span className="font-semibold">Phone:</span>{' '}
                  {user.phone_no || 'N/A'}
                </p>
              </div>
            </div>

            <div>
              <h2 className="text-xl font-semibold mb-4">Statistics</h2>
              <div className="space-y-2">
                <p>
                  <span className="font-semibold">Trust Score:</span>{' '}
                  {user.trustScore || 0}
                </p>
                <p>
                  <span className="font-semibold">Total Listings:</span>{' '}
                  {user.totalListings || 0}
                </p>
                <p>
                  <span className="font-semibold">Successful Trades:</span>{' '}
                  {user.successfulTrades || 0}
                </p>
                {(user.latitude || user.longitude) && (
                  <p>
                    <span className="font-semibold">Location:</span>{' '}
                    {user.latitude?.toFixed(4)}, {user.longitude?.toFixed(4)}
                  </p>
                )}
              </div>
            </div>
          </div>

          {/* TODO: Add cart and wishlist links when those pages are implemented */}
          <div className="mt-6 pt-6 border-t">
            <h2 className="text-xl font-semibold mb-4">Quick Actions</h2>
            <div className="flex gap-4">
              <Link
                to={`/users/${userId}/cart`}
                className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
              >
                View Cart
              </Link>
              <Link
                to={`/users/${userId}/wishlist`}
                className="bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"
              >
                View Wishlist
              </Link>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default UserDetailPage;

