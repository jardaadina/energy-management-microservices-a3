import { AuthProvider, useAuth } from './contexts/AuthContext';
import { LoginPage } from './components/LoginPage';
import { AdminDashboard } from './components/AdminDashboard';
import { UserDashboard } from './components/UserDashboard';
import { Toaster } from './components/ui/sonner';

const AppContent = () => {
  const { currentUser } = useAuth();

  if (!currentUser) {
    return <LoginPage />;
  }

  if (currentUser.role === 'admin') {
    return <AdminDashboard />;
  }

  return <UserDashboard />;
};

export default function App() {
  return (
    <AuthProvider >
      <AppContent />
      <Toaster />
    </AuthProvider>
  );
}
