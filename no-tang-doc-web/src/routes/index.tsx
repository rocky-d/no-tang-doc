import {Routes, Route, Navigate} from 'react-router-dom';
import {Suspense} from 'react';
import {HomePage} from '@/pages/HomePage';
import {LoginPage} from '@/pages/LoginPage';
import {RegisterPage} from '@/pages/RegisterPage';
import {DashboardPage} from '@/pages/DashboardPage';
import {UploadDocumentPage} from '@/pages/UploadDocumentPage';
import {useNavigate} from 'react-router-dom';
import {DocumentsPage} from "@/pages/DocumentsPage.tsx";
import {NotFoundPage} from "@/pages/NotFoundPage.tsx";
import {ProfilePage} from "@/pages/ProfilePage.tsx";
import {TagManagementPage} from "@/pages/TagManagementPage.tsx";
import {MyTeamsPage} from "../pages/MyTeamsPage.tsx";
import {TeamSpacePage} from "../pages/TeamSpacePage.tsx";
import {useAuth} from "../components/AuthContext.tsx";
import { AuthCallbackPage } from '../pages/AuthCallbackPage';

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
// Protected Route wrapper
function ProtectedRoute({ children }: { children: React.ReactNode }) {
    const { isAuthenticated, isLoading } = useAuth();

    if (isLoading) {
        return <div style={{ padding: '2rem', textAlign: 'center' }}>Checking authentication...</div>;
    }
    if (!isAuthenticated) {
        // 如果刚刚执行过登出，直接回首页，避免跳到 /login 触发自动 OIDC 流程
        const recentLogoutTs = Number(sessionStorage.getItem('recent_logout') || '0');
        const justLoggedOut = recentLogoutTs && Date.now() - recentLogoutTs < 8000; // 8秒窗口
        if (justLoggedOut) {
            return <Navigate to="/" replace />;
        }
        // 默认也返回首页，由用户主动点击登录
        return <Navigate to="/" replace />;
    }
    return <>{children}</>;
}

// Route wrapper to redirect authenticated users from auth pages
function AuthRoute({ children }: { children: React.ReactNode }) {
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
                <Route path="*" element={<NotFoundPage/>}/>
            </Routes>
        </Suspense>
    );
}