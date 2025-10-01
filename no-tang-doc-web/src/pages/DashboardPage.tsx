import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../components/AuthContext';
import { Dashboard } from '../components/Dashboard';

export function DashboardPage() {
    const { user } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (!user) navigate('/login', { replace: true });
    }, [user, navigate]);

    if (!user) return null;

    return (
        <Dashboard
            onNavigateHome={() => navigate('/')}
            onNavigateToUpload={() => navigate('/upload')}
        />
    );
}