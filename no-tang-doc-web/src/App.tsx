import React from 'react';

import { BrowserRouter } from 'react-router-dom';
import './index.css';
import { AppRoutes } from './routes';
import { ThemeProvider } from './components/ThemeContext';
import { AuthProvider } from './components/AuthContext';
import { Toaster } from './components/ui/sonner';

export default function App() {
    return (
        <React.StrictMode>
            <BrowserRouter>
                <ThemeProvider>
                    <AuthProvider>
                        <AppRoutes/>
                        <Toaster/>
                    </AuthProvider>
                </ThemeProvider>
            </BrowserRouter>
        </React.StrictMode>
    );
}