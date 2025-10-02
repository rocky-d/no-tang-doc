import { useEffect } from 'react';
import { useAuth } from '../components/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';

export function RegisterPage() {
    const { isAuthenticated, register, isLoading } = useAuth();
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
            register(redirect);
        }
    }, [isAuthenticated, isLoading, navigate, location, register]);

    return <div style={{ padding: '2rem', textAlign: 'center' }}>Redirecting to authorization server (register)...</div>;
}