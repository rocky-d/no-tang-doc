// src/pages/UploadPage.tsx
import { useEffect } from 'react';
import { useAuth } from '../components/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import { UploadPage as UploadPageInner } from '../components/UploadPage';

export function UploadPage() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    // 未登录 -> 登录页并带 redirect
    useEffect(() => {
        if (!user) {
            const params = new URLSearchParams();
            params.set('redirect', location.pathname + location.search);
            navigate(`/login?${params.toString()}`, { replace: true });
        }
    }, [user, navigate, location]);

    if (!user) return null;

    return (
        <UploadPageInner
            onBack={() => navigate('/dashboard')}
            onNavigateHome={() => navigate('/')}
        />
    );
}