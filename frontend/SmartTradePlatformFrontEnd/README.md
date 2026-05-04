# Smart Trade Platform - Frontend

A modern React frontend for the Smart Trade Platform built with Vite, Tailwind CSS, React Router, and Axios.

## Features

- 🛍️ **Items Management**: Browse, create, edit, and delete items
- 👥 **User Management**: Manage user profiles and information
- 🛒 **Shopping Cart**: Add and manage items in cart
- ❤️ **Wishlist**: Save items to wishlist
- 🤝 **Trade Offers**: Create trade offers between users
- 🔍 **Search & Filter**: Search items by name, category, and labels
- 📍 **Location-based**: Filter items by location (when coordinates are provided)

## Tech Stack

- **React 18** - UI library
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Utility-first CSS framework
- **React Router** - Client-side routing
- **Axios** - HTTP client for API requests

## Getting Started

### Prerequisites

- Node.js (v16 or higher)
- npm or yarn
- Backend API running on `http://localhost:8080`

### Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

3. Open your browser and navigate to `http://localhost:3000`

### Building for Production

```bash
npm run build
```

The built files will be in the `dist` folder.

## Project Structure

```
frontend2/
├── src/
│   ├── components/        # Reusable components
│   │   ├── Layout.jsx
│   │   ├── ErrorAlert.jsx
│   │   ├── SuccessAlert.jsx
│   │   └── LoadingSpinner.jsx
│   ├── pages/             # Page components
│   │   ├── HomePage.jsx
│   │   ├── ItemsListPage.jsx
│   │   ├── ItemDetailPage.jsx
│   │   ├── ItemFormPage.jsx
│   │   ├── UsersListPage.jsx
│   │   ├── UserDetailPage.jsx
│   │   ├── UserFormPage.jsx
│   │   ├── CartPage.jsx
│   │   ├── WishlistPage.jsx
│   │   └── TradePage.jsx
│   ├── services/          # API service layer
│   │   ├── api.js         # Axios configuration
│   │   ├── itemService.js
│   │   ├── userService.js
│   │   ├── cartService.js
│   │   ├── wishlistService.js
│   │   └── tradeService.js
│   ├── App.jsx            # Main app component with routing
│   ├── main.jsx           # Entry point
│   └── index.css          # Global styles with Tailwind
├── package.json
├── vite.config.js
├── tailwind.config.js
└── README.md
```

## API Endpoints Covered

### Items
- `GET /items` - Get all items
- `GET /items/{itemId}` - Get item by ID
- `POST /items` - Create new item
- `PATCH /items/{itemId}` - Update item
- `DELETE /items/{itemId}` - Delete item
- `GET /items/nearby` - Get nearby items (location-based)
- `GET /items/search` - Search items by filters
- `PATCH /items/{itemId}/availability` - Toggle item availability

### Users
- `GET /users` - Get all users
- `GET /users/{userId}` - Get user by ID
- `POST /users` - Create new user
- `PATCH /users/{userId}` - Update user
- `DELETE /users/{userId}` - Delete user

### Cart
- `GET /user/{userId}/cart` - Get user's cart
- `POST /user/{userId}/cart/{itemId}` - Add item to cart
- `DELETE /user/{userId}/cart/{itemId}` - Remove item from cart

### Wishlist
- `GET /user/{userId}/wishlist` - Get user's wishlist
- `POST /user/{userId}/wishlist/{itemId}` - Add item to wishlist
- `DELETE /user/{userId}/wishlist/{itemId}` - Remove item from wishlist

### Trades
- `POST /trade/offer` - Create trade offer
- `PATCH /trade/{tradeId}/status` - Update trade status

## Usage Notes

1. **User ID**: Since authentication (JWT) is not yet implemented, you need to enter a User ID in the navigation bar to use cart and wishlist features.

2. **API Base URL**: The default API base URL is `http://localhost:8080`. You can modify this in `src/services/api.js` if your backend runs on a different port.

3. **Future Implementation**: Look for `TODO` comments in the code for features that will be added when JWT authentication is implemented.

## Development

The project uses ESLint for code linting. You can run the linter with:

```bash
npm run lint
```

## License

This project is part of the Smart Trade Platform.

