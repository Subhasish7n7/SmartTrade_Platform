# Quick Start Guide

## Installation & Setup

1. **Navigate to the frontend2 directory:**
   ```bash
   cd frontend2
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Make sure your backend is running on port 8080**

4. **Start the development server:**
   ```bash
   npm run dev
   ```

5. **Open your browser to:**
   ```
   http://localhost:3000
   ```

## First Steps

1. **Create a User:**
   - Go to "Users" → "Add New User"
   - Fill in the form (at minimum: email is required)
   - Save the user and note the User ID

2. **Set Current User:**
   - Enter the User ID in the navigation bar (top right)
   - This is needed for cart and wishlist features (until JWT is implemented)

3. **Create an Item:**
   - Go to "Items" → "Add New Item"
   - Fill in item details
   - Assign the item to a user
   - Save

4. **Browse and Test:**
   - View items in the Items page
   - Click on an item to see details
   - Add items to cart or wishlist
   - Create trade offers

## Important Notes

- **User ID Required**: For cart and wishlist features, you must enter a User ID in the navigation bar
- **API Connection**: Ensure your backend is running on `http://localhost:8080`
- **CORS**: Make sure your backend allows CORS requests from `http://localhost:3000`

## Troubleshooting

- **API Errors**: Check browser console for detailed error messages
- **CORS Issues**: Update your Spring Boot `WebConfig` to allow frontend origin
- **Port Conflicts**: Change the port in `vite.config.js` if 3000 is taken

