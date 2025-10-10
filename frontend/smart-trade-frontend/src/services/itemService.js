import api from './api';

export const fetchAllItems = () => api.get('/items');
export const fetchItemById = (id) => api.get(`/items/${id}`);
export const fetchNearbyItems = (lat, lng, radiusKm = 10) =>
  api.get(`/items/nearby`, { params: { lat, lng, radiusKm } });
export const searchItems = (params) => api.get('/items/search', { params });
export const createItem = (item) => api.post('/items', item);
