import React, { useState, useEffect } from 'react';
import { Header } from "./components/Header";
import { Hero } from "./components/Hero";
import { Features } from "./components/Features";
import { AuthPage } from "./components/AuthPage";
import { Dashboard } from "./components/Dashboard";
import { UploadPage } from "./components/UploadPage";
import { Footer } from "./components/Footer";
import { AuthProvider, useAuth } from "./components/AuthContext";
import { ThemeProvider } from "./components/ThemeContext";
import { Toaster } from "./components/ui/sonner";

type Page = 'landing' | 'auth' | 'dashboard' | 'upload';
type AuthMode = 'login' | 'register';

function AppContent() {
  const { user } = useAuth();
  const [currentPage, setCurrentPage] = useState<Page>('landing');
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [pendingUpload, setPendingUpload] = useState(false);

  // Handle navigation when user logs in and has pending upload
  useEffect(() => {
    if (user && pendingUpload) {
      setCurrentPage('upload');
      setPendingUpload(false);
    } else if (user && currentPage === 'auth') {
      setCurrentPage('dashboard');
    }
  }, [user, pendingUpload, currentPage]);

  const handleNavigateToAuth = (mode: AuthMode) => {
    setAuthMode(mode);
    setCurrentPage('auth');
  };

  const handleNavigateToDashboard = () => {
    setCurrentPage('dashboard');
  };

  const handleBackToLanding = () => {
    setCurrentPage('landing');
  };

  const handleBackToDashboard = () => {
    setCurrentPage('dashboard');
  };

  const handleNavigateHome = () => {
    setCurrentPage('landing');
  };

  const handleNavigateToUpload = () => {
    if (user) {
      setCurrentPage('upload');
    } else {
      setPendingUpload(true);
      setAuthMode('login');
      setCurrentPage('auth');
    }
  };

  const handleStartUploading = () => {
    handleNavigateToUpload();
  };

  // Show upload page
  if (currentPage === 'upload' && user) {
    return <UploadPage onBack={handleBackToDashboard} onNavigateHome={handleNavigateHome} />;
  }

  // Show dashboard
  if (currentPage === 'dashboard' && user) {
    return <Dashboard 
      onNavigateHome={handleNavigateHome} 
      onNavigateToUpload={handleNavigateToUpload}
    />;
  }

  // Show auth page
  if (currentPage === 'auth') {
    return <AuthPage initialMode={authMode} onBack={handleBackToLanding} />;
  }

  // Show landing page (default)
  return (
    <div className="min-h-screen bg-background">
      <Header 
        onNavigateToAuth={handleNavigateToAuth}
        onNavigateToDashboard={handleNavigateToDashboard}
        onNavigateHome={handleNavigateHome}
      />
      <main>
        <Hero onStartUploading={handleStartUploading} />
        <Features />
      </main>
      <Footer />
    </div>
  );
}

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <AppContent />
        <Toaster />
      </AuthProvider>
    </ThemeProvider>
  );
}