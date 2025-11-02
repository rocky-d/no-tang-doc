import { Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import {Suspense} from 'react';
import {HomePage} from '../pages/HomePage';
import {LoginPage} from '../pages/LoginPage';
import {RegisterPage} from '../pages/RegisterPage';
import {DashboardPage} from '../pages/DashboardPage';
import {UploadDocumentPage} from '../pages/UploadDocumentPage';
import {DocumentsPage} from "../pages/DocumentsPage";
import {NotFoundPage} from "../pages/NotFoundPage";
import {ProfilePage} from "../pages/ProfilePage";
import {TagManagementPage} from "../pages/TagManagementPage";
import {MyTeamsPage} from "../pages/MyTeamsPage";
import {TeamSpacePage} from "../pages/TeamSpacePage";
import {useAuth} from "../components/AuthContext";
import { AuthCallbackPage } from '../pages/AuthCallbackPage';
import {LogsPage} from "../pages/LogsPage";

function HomeRoute() {
    const navigate = useNavigate();
    return (
        <HomePage
            onNavigateToAuth={(mode: 'login' | 'register') => navigate(mode === 'login' ? '/login' : '/register')}
            onNavigateToDashboard={() => navigate('/dashboard')}
            onNavigateHome={() => navigate('/')}
            onStartUploading={() => navigate('/upload')}
        />
    );
}

function NotFoundRoute() {
    const navigate = useNavigate();
    return (
        <NotFoundPage
            onNavigateToAuth={(mode: 'login' | 'register') => navigate(mode === 'login' ? '/login' : '/register')}
            onNavigateToDashboard={() => navigate('/dashboard')}
            onNavigateHome={() => navigate('/')}
            onStartUploading={() => navigate('/upload')}
        />
    );
}

// Protected Route wrapper
function ProtectedRoute({ children }: { readonly children: React.ReactNode }) {
    const { isAuthenticated, isLoading } = useAuth();
    const location = useLocation();

    if (isLoading) {
        return <div style={{ padding: '2rem', textAlign: 'center' }}>Checking authentication...</div>;
    }
    if (!isAuthenticated) {
        const target = location.pathname + (location.search || '') + (location.hash || '');
        const to = `/login?redirect=${encodeURIComponent(target)}`;
        return <Navigate to={to} replace />;
    }
    return <>{children}</>;
}

// Route wrapper to redirect authenticated users from auth pages
function AuthRoute({ children }: { readonly children: React.ReactNode }) {
    const { isAuthenticated, isLoading } = useAuth();
    if (isLoading) {
        return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading...</div>;
    }
    if (isAuthenticated) {
        return <Navigate to="/dashboard" replace />;
    }
    return <>{children}</>;
}

export function AppRoutes() {
    return (
        <Suspense fallback={<div>Loading...</div>}>
            <Routes>
                {/* OAuth callback (public) */}
                <Route path="/auth/callback" element={<AuthCallbackPage/>} />
                {/* Public routes */}
                <Route path="/" element={<HomeRoute/>}/>
                <Route path="/login" element={
                    <AuthRoute>
                        <LoginPage/>
                    </AuthRoute>
                }/>
                <Route path="/register" element={
                    <AuthRoute>
                        <RegisterPage/>
                    </AuthRoute>
                }/>
                {/* Protected routes */}
                <Route path="/dashboard" element={
                    <ProtectedRoute>
                        <DashboardPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/documents" element={
                    <ProtectedRoute>
                        <DocumentsPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/upload" element={
                    <ProtectedRoute>
                        <UploadDocumentPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/profile" element={
                    <ProtectedRoute>
                        <ProfilePage/>
                    </ProtectedRoute>
                }/>
                <Route path="/manage/tags" element={
                    <ProtectedRoute>
                        <TagManagementPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/manage/logs" element={
                    <ProtectedRoute>
                        <LogsPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/teams/my-teams" element={
                    <ProtectedRoute>
                        <MyTeamsPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/teams/team-space" element={
                    <ProtectedRoute>
                        <TeamSpacePage/>
                    </ProtectedRoute>
                }/>
                {/* Invalid route */}
                <Route path="*" element={<NotFoundRoute/>}/>
            </Routes>
        </Suspense>
    );
}