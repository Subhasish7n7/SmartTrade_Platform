import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import HomePage from './pages/HomePage';
import ItemDetailPage from './pages/ItemDetailPage';
import ItemFormPage from './pages/ItemFormPage';
import UsersListPage from './pages/UsersListPage';
import UserDetailPage from './pages/UserDetailPage';
import UserProfilePage from './pages/UserProfilePage';
import UserFormPage from './pages/UserFormPage';
import CartPage from './pages/CartPage';
import WishlistPage from './pages/WishlistPage';
import TradePage from './pages/TradePage';
import LoginPage from './pages/LoginPage';
import PaymentMethodsPage from './pages/PaymentMethodsPage';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<HomePage />} />
          
          {/* Items Routes */}
          <Route path="/items/new" element={<ItemFormPage />} />
          <Route path="/items/:itemId" element={<ItemDetailPage />} />
          <Route path="/items/:itemId/edit" element={<ItemFormPage />} />
          
          {/* Users Routes */}
          <Route path="/users" element={<UsersListPage />} />
          <Route path="/users/new" element={<UserFormPage />} />
          <Route path="/users/:userId" element={<UserDetailPage />} />
          <Route path="/users/:userId/edit" element={<UserFormPage />} />
          <Route path="/users/:userId/cart" element={<CartPage />} />
          <Route path="/users/:userId/wishlist" element={<WishlistPage />} />
          
          {/* Direct Cart/Wishlist Routes */}
          <Route path="/cart" element={<CartPage />} />
          <Route path="/wishlist" element={<WishlistPage />} />
          
          {/* Profile Route */}
          <Route path="/profile" element={<UserProfilePage />} />
          
          {/* Trades Route */}
          <Route path="/trades" element={<TradePage />} />
          
          {/* Payment Methods Route */}
          <Route path="/payment-methods" element={<PaymentMethodsPage />} />
          
          {/* Auth Routes */}
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;

