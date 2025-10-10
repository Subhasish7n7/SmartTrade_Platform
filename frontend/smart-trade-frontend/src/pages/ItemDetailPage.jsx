import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { fetchItemById } from '../services/itemService';

export default function ItemDetailPage() {
  const { id } = useParams();
  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchItemById(id)
      .then(response => {
        setItem(response.data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Error fetching item:', error);
        setLoading(false);
      });
  }, [id]);

  if (loading) return <div className="p-4">Loading item...</div>;
  if (!item) return <div className="p-4 text-red-500">Item not found.</div>;

  return (
    <div className="p-4 max-w-2xl mx-auto">
      <h1 className="text-3xl font-bold mb-4">{item.item_name}</h1>
      <p className="mb-2 text-gray-700">{item.description}</p>
      <p className="text-sm text-gray-500">Category: {item.category}</p>
      <p className="text-sm text-gray-500">Condition: {item.condition}</p>
      <p className="text-sm text-gray-500">Price: ${item.item_UserPrice}</p>
      <p className="text-sm text-gray-500">
        For Trade: {item.forTrade ? 'Yes' : 'No'} | For Sale: {item.forSale ? 'Yes' : 'No'}
      </p>
    </div>
  );
}
