import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Layout from '../components/Layout';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorAlert from '../components/ErrorAlert';
import SuccessAlert from '../components/SuccessAlert';
import { useAuth } from '../context/AuthContext';
import { itemService } from '../services/itemService';
import { getCurrentLocation } from '../utils/geolocation';

const ItemFormPage = () => {
  const { itemId } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const isEdit = !!itemId;

  const [loading, setLoading] = useState(isEdit);
  const [locationLoading, setLocationLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const [formData, setFormData] = useState({
    item_name: '',
    description: '',
    category: '',
    condition: '',
    item_UserPrice: '',
    labels: '',
    latitude: '',
    longitude: '',
    available: true,
    forTrade: false,
    forSale: true,
  });

  useEffect(() => {
    // Get location automatically when creating new item
    if (!isEdit) {
      requestLocation();
    }

    if (isEdit) {
      loadItem();
    }
  }, [itemId, isEdit]);

  const requestLocation = async () => {
    setLocationLoading(true);
    try {
      const location = await getCurrentLocation();
      setFormData((prev) => ({
        ...prev,
        latitude: location.latitude.toString(),
        longitude: location.longitude.toString(),
      }));
    } catch (err) {
      console.error('Failed to get location:', err);
      setError(
        'Failed to get your location. Please allow location access or enter coordinates manually.'
      );
    } finally {
      setLocationLoading(false);
    }
  };

  const loadItem = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await itemService.getItem(itemId);
      setFormData({
        item_name: data.item_name || '',
        description: data.description || '',
        category: data.category || '',
        condition: data.condition || '',
        item_UserPrice: data.item_UserPrice || '',
        labels: data.labels ? data.labels.join(', ') : '',
        latitude: data.latitude ? data.latitude.toString() : '',
        longitude: data.longitude ? data.longitude.toString() : '',
        available: data.available !== undefined ? data.available : true,
        forTrade: data.forTrade || false,
        forSale: data.forSale !== undefined ? data.forSale : true,
      });
    } catch (err) {
      setError('Failed to load item. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    // Check authentication
    if (!isEdit && !isAuthenticated()) {
      setError('Please login to create an item.');
      navigate('/login?redirect=/items/new');
      return;
    }

    // Validate location
    if (!formData.latitude || !formData.longitude) {
      setError('Location is required. Please allow location access or enter coordinates.');
      return;
    }

    try {
      // Prepare data - only send item_UserPrice, backend will generate others
      const submitData = {
        item_name: formData.item_name,
        description: formData.description || null,
        category: formData.category || null,
        condition: formData.condition || null,
        item_UserPrice: parseInt(formData.item_UserPrice) || 0,
        // Don't send item_NewPrice or item_GeneratedPrice - backend generates these
        // Set them to 0 to avoid validation issues (backend will override)
        item_NewPrice: 0,
        item_GeneratedPrice: 0,
        labels: formData.labels
          ? formData.labels.split(',').map((l) => l.trim()).filter((l) => l)
          : [],
        latitude: parseFloat(formData.latitude),
        longitude: parseFloat(formData.longitude),
        available: formData.available,
        forTrade: formData.forTrade,
        forSale: formData.forSale,
        // Use current logged-in user for new items
        user: isEdit ? undefined : user, // Backend will use this user object
      };

      if (isEdit) {
        await itemService.updateItem(itemId, submitData);
        setSuccess('Item updated successfully!');
      } else {
        await itemService.createItem(submitData);
        setSuccess('Item created successfully!');
      }

      setTimeout(() => {
        navigate('/items');
      }, 1500);
    } catch (err) {
      console.error('Error:', err);
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data ||
        err.message ||
        (isEdit
          ? 'Failed to update item. Please check all fields and try again.'
          : 'Failed to create item. Please check all fields and try again.');
      setError(errorMessage);
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
          to={isEdit ? `/items/${itemId}` : '/items'}
          className="text-blue-600 hover:underline mb-4 inline-block"
        >
          ← Back
        </Link>

        <h1 className="text-3xl font-bold text-gray-900 mb-6">
          {isEdit ? 'Edit Item' : 'Create New Listing'}
        </h1>

        <ErrorAlert message={error} onClose={() => setError(null)} />
        <SuccessAlert message={success} onClose={() => setSuccess(null)} />

        {/* Location permission notification */}
        {!isEdit && locationLoading && (
          <div className="bg-blue-50 border border-blue-200 text-blue-800 px-4 py-3 rounded mb-4">
            <p className="text-sm">
              🔒 Requesting location permission... Please allow access in your browser.
            </p>
          </div>
        )}

        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-lg shadow-md p-6 max-w-2xl"
        >
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">
                Item Name *
              </label>
              <input
                type="text"
                name="item_name"
                value={formData.item_name}
                onChange={handleChange}
                required
                className="w-full border rounded px-3 py-2"
                placeholder="e.g., iPhone 13 Pro"
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">
                Description
              </label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                rows="3"
                className="w-full border rounded px-3 py-2"
                placeholder="Describe your item..."
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">
                  Category
                </label>
                <input
                  type="text"
                  name="category"
                  value={formData.category}
                  onChange={handleChange}
                  className="w-full border rounded px-3 py-2"
                  placeholder="e.g., Electronics, Books"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">
                  Condition
                </label>
                <select
                  name="condition"
                  value={formData.condition}
                  onChange={handleChange}
                  className="w-full border rounded px-3 py-2"
                >
                  <option value="">Select condition</option>
                  <option value="new">New</option>
                  <option value="used - like new">Used - Like New</option>
                  <option value="used - good">Used - Good</option>
                  <option value="used - fair">Used - Fair</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">
                Your Price ($) *
              </label>
              <input
                type="number"
                name="item_UserPrice"
                value={formData.item_UserPrice}
                onChange={handleChange}
                min="0"
                required
                className="w-full border rounded px-3 py-2"
                placeholder="Enter your asking price"
              />
              <p className="text-xs text-gray-500 mt-1">
                * New price and generated price will be calculated automatically by the system
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">
                Labels (comma separated)
              </label>
              <input
                type="text"
                name="labels"
                value={formData.labels}
                onChange={handleChange}
                placeholder="e.g., gaming, intel, DDR4"
                className="w-full border rounded px-3 py-2"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">
                  Latitude *
                </label>
                <input
                  type="number"
                  name="latitude"
                  value={formData.latitude}
                  onChange={handleChange}
                  step="any"
                  required
                  readOnly={!isEdit && !locationLoading && formData.latitude}
                  className="w-full border rounded px-3 py-2"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">
                  Longitude *
                </label>
                <input
                  type="number"
                  name="longitude"
                  value={formData.longitude}
                  onChange={handleChange}
                  step="any"
                  required
                  readOnly={!isEdit && !locationLoading && formData.longitude}
                  className="w-full border rounded px-3 py-2"
                />
              </div>
            </div>
            {!isEdit && (
              <div className="flex items-center justify-between bg-gray-50 p-3 rounded">
                <span className="text-sm text-gray-600">
                  Location is automatically filled from your browser
                </span>
                <button
                  type="button"
                  onClick={requestLocation}
                  className="text-sm text-blue-600 hover:underline"
                >
                  Refresh Location
                </button>
              </div>
            )}

            <div className="flex gap-6">
              <label className="flex items-center">
                <input
                  type="checkbox"
                  name="available"
                  checked={formData.available}
                  onChange={handleChange}
                  className="mr-2"
                />
                Available
              </label>

              <label className="flex items-center">
                <input
                  type="checkbox"
                  name="forTrade"
                  checked={formData.forTrade}
                  onChange={handleChange}
                  className="mr-2"
                />
                For Trade
              </label>

              <label className="flex items-center">
                <input
                  type="checkbox"
                  name="forSale"
                  checked={formData.forSale}
                  onChange={handleChange}
                  className="mr-2"
                />
                For Sale
              </label>
            </div>

            {!isEdit && isAuthenticated() && (
              <div className="bg-blue-50 border border-blue-200 p-3 rounded text-sm text-blue-800">
                Item will be listed under: <strong>{user?.name || user?.email}</strong>
              </div>
            )}

            <div className="flex gap-4 pt-4">
              <button
                type="submit"
                className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
              >
                {isEdit ? 'Update Item' : 'Create Listing'}
              </button>
              <Link
                to={isEdit ? `/items/${itemId}` : '/items'}
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

export default ItemFormPage;
