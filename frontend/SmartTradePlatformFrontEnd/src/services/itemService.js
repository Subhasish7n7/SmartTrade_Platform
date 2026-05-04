import api from './api';

export const itemService = {
  // Get all items
  getAllItems: async () => {
    const response = await api.get('/items');
    return response.data;
  },

  // Get item by ID
  getItem: async (itemId) => {
    const response = await api.get(`/items/${itemId}`);
    return response.data;
  },

  // Create item
  createItem: async (itemData) => {
    const response = await api.post('/items', itemData);
    return response.data;
  },

  // Update item
  updateItem: async (itemId, itemData) => {
    const response = await api.patch(`/items/${itemId}`, itemData);
    return response.data;
  },

  // Delete item
  deleteItem: async (itemId) => {
    await api.delete(`/items/${itemId}`);
  },

  // Get nearby items
  getNearbyItems: async (lat, lng, radiusKm = 10) => {
    const response = await api.get('/items/nearby', {
      params: { lat, lng, radiusKm },
    });
    return response.data;
  },

  // Search items
  searchItems: async (filters = {}) => {
    const params = {};
    if (filters.category) params.category = filters.category;
    if (filters.name) params.name = filters.name;
    if (filters.labels && filters.labels.length > 0) {
      params.labels = filters.labels;
    }
    const response = await api.get('/items/search', { params });
    return response.data;
  },

  // Toggle item availability
  toggleAvailability: async (itemId, available) => {
    await api.patch(`/items/${itemId}/availability`, null, {
      params: { available },
    });
  },
};

