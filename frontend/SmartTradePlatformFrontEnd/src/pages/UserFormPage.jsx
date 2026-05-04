import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Layout from '../components/Layout';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorAlert from '../components/ErrorAlert';
import SuccessAlert from '../components/SuccessAlert';
import { userService } from '../services/userService';

const UserFormPage = () => {
  const { userId } = useParams();
  const navigate = useNavigate();
  const isEdit = !!userId;

  const [loading, setLoading] = useState(isEdit);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const [formData, setFormData] = useState({
    email: '',
    name: '',
    phone_no: '',
    latitude: '',
    longitude: '',
    trustScore: 0,
    totalListings: 0,
    successfulTrades: 0,
  });

  useEffect(() => {
    if (isEdit) {
      loadUser();
    }
  }, [userId]);

  const loadUser = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await userService.getUser(userId);
      setFormData({
        email: data.email || '',
        name: data.name || '',
        phone_no: data.phone_no || '',
        latitude: data.latitude || '',
        longitude: data.longitude || '',
        trustScore: data.trustScore || 0,
        totalListings: data.totalListings || 0,
        successfulTrades: data.successfulTrades || 0,
      });
    } catch (err) {
      setError('Failed to load user. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    try {
      const submitData = {
        ...formData,
        trustScore: parseFloat(formData.trustScore) || 0,
        totalListings: parseInt(formData.totalListings) || 0,
        successfulTrades: parseInt(formData.successfulTrades) || 0,
        latitude: formData.latitude ? parseFloat(formData.latitude) : null,
        longitude: formData.longitude ? parseFloat(formData.longitude) : null,
      };

      if (isEdit) {
        await userService.updateUser(userId, submitData);
        setSuccess('User updated successfully!');
      } else {
        await userService.createUser(submitData);
        setSuccess('User created successfully!');
      }

      setTimeout(() => {
        navigate('/users');
      }, 1500);
    } catch (err) {
      setError(
        isEdit
          ? 'Failed to update user. Please try again.'
          : 'Failed to create user. Please try again.'
      );
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
          to={isEdit ? `/users/${userId}` : '/users'}
          className="text-blue-600 hover:underline mb-4 inline-block"
        >
          ← Back
        </Link>

        <h1 className="text-3xl font-bold text-gray-900 mb-6">
          {isEdit ? 'Edit User' : 'Create New User'}
        </h1>

        <ErrorAlert message={error} onClose={() => setError(null)} />
        <SuccessAlert message={success} onClose={() => setSuccess(null)} />

        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-lg shadow-md p-6 max-w-2xl"
        >
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">Email *</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
                className="w-full border rounded px-3 py-2"
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Name</label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                className="w-full border rounded px-3 py-2"
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">
                Phone Number
              </label>
              <input
                type="text"
                name="phone_no"
                value={formData.phone_no}
                onChange={handleChange}
                className="w-full border rounded px-3 py-2"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">
                  Latitude
                </label>
                <input
                  type="number"
                  name="latitude"
                  value={formData.latitude}
                  onChange={handleChange}
                  step="any"
                  className="w-full border rounded px-3 py-2"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">
                  Longitude
                </label>
                <input
                  type="number"
                  name="longitude"
                  value={formData.longitude}
                  onChange={handleChange}
                  step="any"
                  className="w-full border rounded px-3 py-2"
                />
              </div>
            </div>

            {isEdit && (
              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-1">
                    Trust Score
                  </label>
                  <input
                    type="number"
                    name="trustScore"
                    value={formData.trustScore}
                    onChange={handleChange}
                    step="0.1"
                    min="0"
                    className="w-full border rounded px-3 py-2"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium mb-1">
                    Total Listings
                  </label>
                  <input
                    type="number"
                    name="totalListings"
                    value={formData.totalListings}
                    onChange={handleChange}
                    min="0"
                    className="w-full border rounded px-3 py-2"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium mb-1">
                    Successful Trades
                  </label>
                  <input
                    type="number"
                    name="successfulTrades"
                    value={formData.successfulTrades}
                    onChange={handleChange}
                    min="0"
                    className="w-full border rounded px-3 py-2"
                  />
                </div>
              </div>
            )}

            <div className="flex gap-4 pt-4">
              <button
                type="submit"
                className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
              >
                {isEdit ? 'Update User' : 'Create User'}
              </button>
              <Link
                to={isEdit ? `/users/${userId}` : '/users'}
                className="bg-gray-300 text-gray-700 px-6 py-2 rounded hover:bg-gray-400"
              >
                Cancel
              </Link>
            </div>
          </div>
        </form>
      </div>
    </Layout>
  );
};

export default UserFormPage;

