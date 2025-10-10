import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import ItemDetailPage from './pages/ItemDetailPage';
import AddItemPage from './pages/AddItemPage';
import { UserProvider } from './context/UserContext';
import { LocationProvider } from './context/LocationContext';

const App = () => {
  return (
    <UserProvider>
      <LocationProvider>
        <Router>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/item/:id" element={<ItemDetailPage />} />
            <Route path="/add-item" element={<AddItemPage />} />
            {/* Add more routes later */}
          </Routes>
        </Router>
      </LocationProvider>
    </UserProvider>
  );
};

export default App;
// Backend:

// Entity classes (e.g., Item, User, Location)

// DTOs (data transfer objects) if any

// API endpoint descriptions (routes, expected request/response formats)

// Services/controllers relevant to UI features

// Frontend:

// What you already have (components, services)

// What you want (e.g., item list page, add item form)

// Any styling or UX requirements
