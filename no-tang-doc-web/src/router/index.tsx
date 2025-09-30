// ========== 修改: src/router/index.tsx ==========
import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import RouteLoading from '../components/RouteLoading';
const HomePage = lazy(() => import('../pages/HomePage'));
const DocumentPage = lazy(() => import('../pages/DocumentPage'));
const DashboardPage = lazy(() => import('../pages/DashboardPage'));
const NotFoundPage = lazy(() => import('../pages/NotFoundPage'));

export function AppRouter() {
    return (
        <BrowserRouter>
            <nav style={{ padding: 12, borderBottom: '1px solid #ddd', display: 'flex', gap: 12 }}>
                <Link to="/">Home</Link>
                <Link to="/document">Document</Link>
                <Link to="/dashboard">Dashboard</Link>
            </nav>
            <Suspense fallback={<RouteLoading />}>
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/document" element={<DocumentPage />} />
                    <Route path="/dashboard" element={<DashboardPage />} />
                    <Route path="*" element={<NotFoundPage />} />
                </Routes>
            </Suspense>
        </BrowserRouter>
    );
}