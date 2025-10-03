import { useEffect } from 'react';
import { useAuth } from '../components/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';

export function RegisterPage() {
    const { isAuthenticated, register, isLoading } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        if (isLoading) return;
        const sp = new URLSearchParams(location.search);
        const redirect = sp.get('redirect') || '/dashboard';
        if (isAuthenticated) {
            navigate(redirect, { replace: true });
            return;
        }
        // 直接触发注册（不再抑制）
        register(redirect);
    }, [isAuthenticated, isLoading, navigate, location, register]);

    return <div style={{ padding: '2rem', textAlign: 'center' }}>Redirecting to authorization server (register)...</div>;
}