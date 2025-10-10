import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchAllItems } from '../services/itemService';

export default function HomePage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    console.log('HomePage loaded'); // Debug log
  }, []);

  useEffect(() => {
    const loadItems = async () => {
      try {
        const response = await fetchAllItems();
        console.log('Fetched items:', response.data); // Debug log
        setItems(Array.isArray(response.data) ? response.data : []);
      } catch (error) {
        console.error('Error fetching items:', error);
        setItems([]); // Prevent `map` crash
      } finally {
        setLoading(false);
      }
    };

    loadItems();
  }, []);

  if (loading) {
    return <div className="p-4 text-gray-500">Loading items...</div>;
  }

  return (
    <div className="p-4 max-w-3xl mx-auto">
      <h1 className="text-3xl font-bold mb-6 text-center">Smart Trade Platform</h1>

      <div className="flex justify-end mb-6">
        <Link
          to="/add-item"
          className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
        >
          Add New Item
        </Link>
      </div>

      {items.length === 0 ? (
        <div className="text-gray-600 text-center">No items found.</div>
      ) : (
        <>
          <h2 className="text-xl font-semibold mb-4">Available Items</h2>
          <ul className="space-y-4">
            {items.map(item => (
              <li
                key={item.itemId}
                className="border p-4 rounded hover:shadow-lg transition"
              >
                <Link to={`/item/${item.itemId}`} className="text-blue-600 hover:underline">
                  <h3 className="text-lg font-bold">{item.item_name}</h3>
                </Link>
                <p className="text-gray-700">{item.description}</p>
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
}
