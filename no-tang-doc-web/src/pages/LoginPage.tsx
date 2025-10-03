import { useEffect } from 'react';
import { useAuth } from '../components/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';

export function LoginPage() {
    const { isAuthenticated, login, isLoading } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        if (isLoading) return;
        if (isAuthenticated) {
            const sp = new URLSearchParams(location.search);
            const redirect = sp.get('redirect') || '/dashboard';
            navigate(redirect, { replace: true });
        } else {
            const sp = new URLSearchParams(location.search);
            const redirect = sp.get('redirect') || '/dashboard';
            login(redirect);
        }
    }, [isAuthenticated, isLoading, navigate, location, login]);

    return <div style={{ padding: '2rem', textAlign: 'center' }}>Redirecting to authorization server (login)...</div>;
}