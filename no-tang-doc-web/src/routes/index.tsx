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
                {/* Public routes */}
                <Route path="/" element={<HomeRoute/>}/>
                <Route path="/login" element={<LoginPage/>}/>
                <Route path="/register" element={<RegisterPage/>}/>
                {/* Public routes */}
                <Route path="/dashboard" element={<DashboardPage/>}/>
                <Route path="/documents" element={<DocumentsPage/>}/>
                <Route path="/upload" element={<UploadDocumentPage/>}/>
                <Route path="/profile" element={<ProfilePage/>}/>
                <Route path="/manage/tags" element={<TagManagementPage/>}/>
                <Route path="/teams/my-teams" element={<MyTeamsPage/>}/>
                <Route path="/teams/team-space" element={<TeamSpacePage/>}/>
                {/* Invalid route */}
                <Route path="*" element={<NotFoundPage/>}/>
                {/* Fallback route  depends on the logic that redirect to home page or 404*/}
                {/*<Route path="*" element={<Navigate to="/" replace />} />*/}
            </Routes>
        </Suspense>
    );
}