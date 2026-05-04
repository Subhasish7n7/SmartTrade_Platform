import Layout from '../components/Layout';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { useEffect } from 'react';

const PaymentMethodsPage = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate('/login?redirect=/payment-methods');
    }
  }, [isAuthenticated, navigate]);

  if (!isAuthenticated()) {
    return null;
  }

  return (
    <Layout>
      <div className="px-4">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">
          Payment Methods
        </h1>

        <div className="bg-white rounded-lg shadow-md p-6">
          <p className="text-gray-500 mb-4">
            Payment methods management will be implemented here.
          </p>
          {/* TODO: Implement payment methods CRUD when backend is ready */}
          <div className="text-center py-8">
            <p className="text-gray-500">No payment methods added yet.</p>
            <button className="mt-4 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
              Add Payment Method
            </button>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default PaymentMethodsPage;

