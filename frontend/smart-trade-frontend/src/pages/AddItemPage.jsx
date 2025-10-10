import React, { useState, useEffect } from 'react';
import { createItem } from '../services/itemService';
import { useUser } from '../context/UserContext';
import { useLocation } from '../context/LocationContext';
import { useNavigate } from 'react-router-dom';

const categories = ['Electronics', 'Books', 'Clothing', 'Misc'];
const conditions = ['new', 'used - like new', 'used - good', 'used - acceptable'];

export default function AddItemPage() {
  const [itemName, setItemName] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('Misc');
  const [condition, setCondition] = useState('used - good');
  const [userPrice, setUserPrice] = useState('');
  const [isForTrade, setIsForTrade] = useState(false);
  const [isForSale, setIsForSale] = useState(true);

  const navigate = useNavigate();
  const { user } = useUser();
  const { location } = useLocation();

  // 🔍 Debug: Log state changes when checkboxes are toggled
  useEffect(() => {
    console.log('🔄 Checkbox state changed: isForTrade =', isForTrade, '| isForSale =', isForSale);
  }, [isForTrade, isForSale]);

  const handleSubmit = (e) => {
    e.preventDefault();

    const newItem = {
      item_name: itemName,
      description,
      category,
      condition,
      item_UserPrice: Number(userPrice) || 0,
      item_NewPrice: 0,           // optional or placeholder
      item_GeneratedPrice: 0,     // optional or placeholder
      forSale: isForSale,
      forTrade: isForTrade,
      available: true,
      labels: [],                 // optional
      user: user || null,
      latitude: location?.latitude || 0,
      longitude: location?.longitude || 0,
    };

    // 🧪 Debug: Confirm what’s being sent to backend
    console.log('🚀 Submitting item:', newItem);

    createItem(newItem)
      .then(() => {
        alert('Item added!');
        navigate('/');
      })
      .catch((error) => {
        console.error('❌ Error adding item:', error);
        alert('Failed to add item');
      });
  };

  return (
    <div className="p-4 max-w-md mx-auto">
      <h1 className="text-2xl font-bold mb-4">Add New Item</h1>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">

        {/* Item Name Input */}
        <input
          type="text"
          placeholder="Item Name"
          value={itemName}
          onChange={e => setItemName(e.target.value)}
          className="border p-2 rounded"
          required
        />

        {/* Description Input */}
        <textarea
          placeholder="Description"
          value={description}
          onChange={e => setDescription(e.target.value)}
          className="border p-2 rounded"
          rows={4}
          required
        />

        {/* Category Dropdown */}
        <select
          value={category}
          onChange={e => setCategory(e.target.value)}
          className="border p-2 rounded"
          required
        >
          {categories.map(cat => (
            <option key={cat} value={cat}>{cat}</option>
          ))}
        </select>

        {/* Condition Dropdown */}
        <select
          value={condition}
          onChange={e => setCondition(e.target.value)}
          className="border p-2 rounded"
          required
        >
          {conditions.map(cond => (
            <option key={cond} value={cond}>{cond}</option>
          ))}
        </select>

        {/* Price Input */}
        <input
          type="number"
          placeholder="Price"
          value={userPrice}
          onChange={e => setUserPrice(e.target.value)}
          className="border p-2 rounded"
          min={0}
          required
        />

        {/* For Trade Checkbox */}
        <label className="flex items-center gap-2">
          <input
            type="checkbox"
            checked={isForTrade}
            onChange={() => setIsForTrade(prev => !prev)} // ✅ FIX: use toggle function
          />
          For Trade
        </label>

        {/* For Sale Checkbox */}
        <label className="flex items-center gap-2">
          <input
            type="checkbox"
            checked={isForSale}
            onChange={() => setIsForSale(prev => !prev)} // ✅ FIX: use toggle function
          />
          For Sale
        </label>

        {/* Submit Button */}
        <button
          type="submit"
          className="bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
        >
          Add Item
        </button>
      </form>
    </div>
  );
}
