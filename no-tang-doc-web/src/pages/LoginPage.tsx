import { useEffect } from 'react';
import { useAuth } from '../components/AuthContext';
import { AuthPage } from '../components/AuthPage';
import { useNavigate, useLocation } from 'react-router-dom';

export function LoginPage() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        if (user) {
            const sp = new URLSearchParams(location.search);
            const redirect = sp.get('redirect');
            navigate(redirect || '/dashboard', { replace: true });
        }
    }, [user, navigate, location.search]);

    return <AuthPage initialMode="login" onBack={() => navigate('/')} />;
}