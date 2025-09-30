import { Routes, Route } from 'react-router-dom';
import { Suspense } from 'react';
import { HomePage } from '@/pages/HomePage';
import { LoginPage } from '@/pages/LoginPage';
import { RegisterPage } from '@/pages/RegisterPage';
import { DashboardPage } from '@/pages/DashboardPage';
import { UploadPage } from '@/pages/UploadPage';
import { useNavigate } from 'react-router-dom';

function HomeRoute() {
    const navigate = useNavigate();
    return (
        <HomePage
            onNavigateToAuth={(mode) => navigate(mode === 'login' ? '/login' : '/register')}
            onNavigateToDashboard={() => navigate('/dashboard')}
            onNavigateHome={() => navigate('/')}
            onStartUploading={() => navigate('/upload')}
        />
    );
}

export function AppRoutes() {
    return (
        <Suspense fallback={<div>Loading...</div>}>
            <Routes>
                <Route path="/" element={<HomeRoute />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/upload" element={<UploadPage />} />
                <Route path="*" element={<div>Not Found</div>} />
            </Routes>
        </Suspense>
    );
}