import { Button } from "./ui/button";
import { FileText, LogIn, UserPlus, LayoutDashboard } from "lucide-react";
import { useAuth } from "./AuthContext";

interface HeaderProps {
  onNavigateToAuth?: (mode: 'login' | 'register') => void;
  onNavigateToDashboard?: () => void;
  onNavigateHome?: () => void;
}

export function Header({ onNavigateToAuth, onNavigateToDashboard, onNavigateHome }: HeaderProps) {
  const { user } = useAuth();

  return (
    <header className="border-b bg-white/95 backdrop-blur supports-[backdrop-filter]:bg-white/60 sticky top-0 z-50">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        <button 
          className="flex items-center space-x-2 hover:opacity-80 transition-opacity"
          onClick={onNavigateHome}
        >
          <FileText className="h-6 w-6 text-primary" />
          <span className="font-semibold">DocRepo</span>
        </button>
        
        <nav className="hidden md:flex items-center space-x-6">
          <a href="/#home" className="text-muted-foreground hover:text-foreground transition-colors">
            Home
          </a>
          <a href="/#features" className="text-muted-foreground hover:text-foreground transition-colors">
            Features
          </a>
        </nav>

        <div className="flex items-center space-x-3">
          {user ? (
            <Button 
              size="sm"
              onClick={onNavigateToDashboard}
              className="flex items-center space-x-2"
            >
              <LayoutDashboard className="h-4 w-4" />
              <span>Dashboard</span>
            </Button>
          ) : (
            <>
              <Button 
                variant="ghost" 
                size="sm"
                onClick={() => onNavigateToAuth?.('login')}
              >
                <LogIn className="h-4 w-4 mr-2" />
                Sign In
              </Button>
              <Button 
                size="sm"
                onClick={() => onNavigateToAuth?.('register')}
              >
                <UserPlus className="h-4 w-4 mr-2" />
                Sign Up
              </Button>
            </>
          )}
        </div>
      </div>
    </header>
  );
}