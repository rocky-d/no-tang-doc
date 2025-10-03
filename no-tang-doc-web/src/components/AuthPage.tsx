import React, { useState } from 'react';
import { ArrowLeft, FileText } from 'lucide-react';
import { Button } from './ui/button';
import { LoginForm } from './LoginForm';
import { RegisterForm } from './RegisterForm';

interface AuthPageProps {
  initialMode?: 'login' | 'register';
  onBack: () => void;
}

export function AuthPage({ initialMode = 'login', onBack }: AuthPageProps) {
  const [isLogin, setIsLogin] = useState(initialMode === 'login');

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b bg-card">
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <Button variant="ghost" onClick={onBack} className="flex items-center space-x-2">
              <ArrowLeft className="w-4 h-4" />
              <span>Back to Home</span>
            </Button>
          </div>
          
          <div className="flex items-center space-x-2">
            <FileText className="h-6 w-6 text-primary" />
            <span className="font-semibold">DocRepo</span>
          </div>
          
          <div className="w-32"></div> {/* Spacer for center alignment */}
        </div>
      </header>

      {/* Auth Content */}
      <main className="flex items-center justify-center min-h-[calc(100vh-4rem)] py-8">
        <div className="w-full max-w-md">
          {isLogin ? (
            <LoginForm onSwitchToRegister={() => setIsLogin(false)} />
          ) : (
            <RegisterForm onSwitchToLogin={() => setIsLogin(true)} />
          )}
        </div>
      </main>
    </div>
  );
}