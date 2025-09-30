// src/routes/index.tsx
import { Routes, Route } from 'react-router-dom';
import HomePage from '../pages/HomePage.tsx';
import AboutPage from '../pages/DashboardPage.tsx';
import App from '../App.tsx';
import DocumentPage from "../pages/DocumentPage.tsx";

export function AppRoutes() {
    return (
        <Routes>
            <Route path="/" element={<HomePage/>}/>
            <Route path="/dashboard" element={<AboutPage/>}/>
            <Route path="/document" element={<DocumentPage/>}/>
            <Route path="/app" element={<App/>}/>
        </Routes>
    );
}