import api from './api';

export const fetchAllUsers = () => api.get('/users');
export const fetchUserById = (id) => api.get(`/users/${id}`);
export const createUser = (user) => api.post('/users', user);
