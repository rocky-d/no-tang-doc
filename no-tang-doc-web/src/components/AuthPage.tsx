import { ArrowLeft, FileText } from 'lucide-react';
import { Button } from './ui/button';
import { useAuth } from './AuthContext';

interface AuthPageProps {
  initialMode?: 'login' | 'register';
  onBack: () => void;
}

export function AuthPage({ initialMode = 'login', onBack }: AuthPageProps) {
  const { login, register, isLoading } = useAuth();
  // Use prop directly so rerender reflects new heading
  const mode: 'login' | 'register' = initialMode;

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
            <span className="font-semibold">NTDoc</span>
          </div>
          
          <div className="w-32"></div>
        </div>
      </header>

      {/* Auth Content */}
      <main className="flex items-center justify-center min-h-[calc(100vh-4rem)] py-8">
        <div className="w-full max-w-md space-y-4 text-center">
          <h2 className="text-xl font-semibold">{mode === 'login' ? 'Sign in' : 'Create your account'}</h2>
          <p className="text-sm text-muted-foreground">You will be redirected to the identity provider.</p>
          <div className="flex flex-col gap-3">
            <Button onClick={() => login()} disabled={isLoading}>
              Sign in
            </Button>
            <Button variant="outline" onClick={() => register()} disabled={isLoading}>
              Create account
            </Button>
          </div>
        </div>
      </main>
    </div>
  );
}