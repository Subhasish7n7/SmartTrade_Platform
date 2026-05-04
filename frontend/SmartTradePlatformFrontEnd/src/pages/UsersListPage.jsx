import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../components/Layout';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorAlert from '../components/ErrorAlert';
import { userService } from '../services/userService';

const UsersListPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await userService.getAllUsers();
      setUsers(data);
    } catch (err) {
      setError('Failed to load users. Please try again.');
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

  return (
    <Layout>
      <div className="px-4">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Users</h1>
          <Link
            to="/users/new"
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          >
            Add New User
          </Link>
        </div>

        <ErrorAlert message={error} onClose={() => setError(null)} />

        {users.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            <p>No users found.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {users.map((user) => (
              <Link
                key={user.userId}
                to={`/users/${user.userId}`}
                className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow p-6"
              >
                <h3 className="text-xl font-semibold mb-2">
                  {user.name || 'No Name'}
                </h3>
                <p className="text-gray-600 text-sm mb-2">{user.email}</p>
                {user.phone_no && (
                  <p className="text-gray-600 text-sm mb-2">
                    📞 {user.phone_no}
                  </p>
                )}
                <div className="mt-4 flex justify-between items-center">
                  <span className="text-sm text-gray-500">
                    Trust Score: {user.trustScore || 0}
                  </span>
                  <span className="text-sm text-gray-500">
                    {user.successfulTrades || 0} trades
                  </span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default UsersListPage;

