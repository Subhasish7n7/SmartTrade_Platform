import api from './api';

export const tradeService = {
  // Create trade offer
  createTradeOffer: async (tradeData) => {
    const response = await api.post('/trade/offer', tradeData);
    return response.data;
  },

  // Update trade status
  updateTradeStatus: async (tradeId, status) => {
    await api.patch(`/trade/${tradeId}/status`, null, {
      params: { status },
    });
  },
};

