import api from './api';

export const cartService = {
  // Get user's cart
  getCart: async (userId) => {
    const response = await api.get(`/user/${userId}/cart`);
    return response.data;
  },

  // Add item to cart
  addToCart: async (userId, itemId, quantity = 1) => {
    const response = await api.post(`/user/${userId}/cart/${itemId}`, null, {
      params: { quantity },
    });
    return response.data;
  },

  // Remove item from cart
  removeFromCart: async (userId, itemId) => {
    await api.delete(`/user/${userId}/cart/${itemId}`);
  },
};

