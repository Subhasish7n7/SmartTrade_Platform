import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../components/Layout';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorAlert from '../components/ErrorAlert';
import { itemService } from '../services/itemService';

const HomePage = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [advancedFilters, setAdvancedFilters] = useState({
    category: '',
    labels: '',
    sortPrice: '',
  });

  useEffect(() => {
    loadItems();
  }, []);

  const loadItems = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await itemService.getAllItems();
      setItems(data);
    } catch (err) {
      setError('Failed to load items. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError(null);

      const filters = {
        name: searchQuery || undefined,
        category: advancedFilters.category || undefined,
        labels: advancedFilters.labels
          ? advancedFilters.labels.split(',').map((l) => l.trim())
          : undefined,
      };

      let data = await itemService.searchItems(filters);

      // Sort by price if selected
      if (advancedFilters.sortPrice === 'low-high') {
        data = [...data].sort(
          (a, b) =>
            (a.item_UserPrice || a.item_NewPrice || 0) -
            (b.item_UserPrice || b.item_NewPrice || 0)
        );
      } else if (advancedFilters.sortPrice === 'high-low') {
        data = [...data].sort(
          (a, b) =>
            (b.item_UserPrice || b.item_NewPrice || 0) -
            (a.item_UserPrice || a.item_NewPrice || 0)
        );
      }

      setItems(data);
    } catch (err) {
      setError('Failed to search items. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleClear = () => {
    setSearchQuery('');
    setAdvancedFilters({ category: '', labels: '', sortPrice: '' });
    setShowAdvanced(false);
    loadItems();
  };

  if (loading && items.length === 0) {
    return (
      <Layout>
        <LoadingSpinner />
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="px-4">
        {/* Search Bar */}
        <div className="mb-6">
          <form onSubmit={handleSearch} className="space-y-4">
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="Search items..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="flex-1 border rounded-lg text-black px-4 py-2 text-lg"
              />
              <button
                type="submit"
                className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
              >
                Search
              </button>
              <button
                type="button"
                onClick={handleClear}
                className="bg-gray-300 text-gray-700 px-6 py-2 rounded-lg hover:bg-gray-400"
              >
                Clear
              </button>
            </div>

            {/* Advanced Search Toggle */}
            <div>
              <button
                type="button"
                onClick={() => setShowAdvanced(!showAdvanced)}
                className="text-blue-600 hover:underline text-sm"
              >
                {showAdvanced ? '▼' : '▶'} Advanced Search
              </button>
            </div>

            {/* Advanced Search Options */}
            {showAdvanced && (
              <div className="bg-white p-4 rounded-lg shadow border space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-medium mb-1">
                      Category
                    </label>
                    <input
                      type="text"
                      placeholder="e.g., Electronics"
                      value={advancedFilters.category}
                      onChange={(e) =>
                        setAdvancedFilters({
                          ...advancedFilters,
                          category: e.target.value,
                        })
                      }
                      className="w-full border rounded px-3 py-2"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-1">
                      Labels (comma separated)
                    </label>
                    <input
                      type="text"
                      placeholder="e.g., gaming, intel"
                      value={advancedFilters.labels}
                      onChange={(e) =>
                        setAdvancedFilters({
                          ...advancedFilters,
                          labels: e.target.value,
                        })
                      }
                      className="w-full border rounded px-3 py-2"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-1">
                      Sort by Price
                    </label>
                    <select
                      value={advancedFilters.sortPrice}
                      onChange={(e) =>
                        setAdvancedFilters({
                          ...advancedFilters,
                          sortPrice: e.target.value,
                        })
                      }
                      className="w-full border rounded px-3 py-2"
                    >
                      <option value="">None</option>
                      <option value="low-high">Low to High</option>
                      <option value="high-low">High to Low</option>
                    </select>
                  </div>
                </div>
              </div>
            )}
          </form>
        </div>

        <ErrorAlert message={error} onClose={() => setError(null)} />

        {/* Items Grid */}
        {items.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            <p className="text-lg">No items found.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {items.map((item) => (
              <Link
                key={item.itemId}
                to={`/items/${item.itemId}`}
                className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow overflow-hidden"
              >
                <div className="p-6">
                  <h3 className="text-xl font-semibold mb-2">
                    {item.item_name}
                  </h3>
                  <p className="text-gray-600 text-sm mb-2 line-clamp-2">
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
                  {item.category && (
                    <span className="inline-block mt-2 px-2 py-1 bg-gray-100 rounded text-xs">
                      {item.category}
                    </span>
                  )}
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default HomePage;
