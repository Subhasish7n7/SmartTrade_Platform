import api from './api';

export const wishlistService = {
  // Get user's wishlist
  getWishlist: async (userId) => {
    const response = await api.get(`/user/${userId}/wishlist`);
    return response.data;
  },

  // Add item to wishlist
  addToWishlist: async (userId, itemId) => {
    const response = await api.post(`/user/${userId}/wishlist/${itemId}`);
    return response.data;
  },

  // Remove item from wishlist
  removeFromWishlist: async (userId, itemId) => {
    await api.delete(`/user/${userId}/wishlist/${itemId}`);
  },
};

