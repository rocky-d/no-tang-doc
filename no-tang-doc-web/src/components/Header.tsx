import { Button } from "./ui/button";
import { FileText, LogIn, UserPlus, LayoutDashboard, User as UserIcon, LogOut } from "lucide-react";
import { useAuth } from "./AuthContext";
import { Avatar, AvatarFallback, AvatarImage } from "./ui/avatar";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger } from "./ui/dropdown-menu";

interface HeaderProps {
  onNavigateToAuth?: (mode: 'login' | 'register') => void;
  onNavigateToDashboard?: () => void;
  onNavigateHome?: () => void;
}

;
export function Header({ onNavigateToAuth, onNavigateToDashboard, onNavigateHome }: HeaderProps) {
  const { user, login, register, logout, isLoading } = useAuth();

  const handleSignIn = () => {
    if (isLoading) return;
    login('/dashboard');
    console.log(user)
  };
  const handleSignUp = () => {
    if (isLoading) return;
    register('/dashboard');

  };

  const getUserInitials = (name?: string) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
  };

  return (
    <header className="border-b bg-white/95 backdrop-blur supports-[backdrop-filter]:bg-white/60 sticky top-0 z-50">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        <button 
          className="flex items-center space-x-2 hover:opacity-80 transition-opacity"
          onClick={onNavigateHome}
        >
          <FileText className="h-6 w-6 text-primary" />
          <span className="font-semibold">NTDoc</span>
        </button>
        
        <nav className="hidden md:flex items-center space-x-6">
          <a href="/#home" className="text-muted-foreground hover:text-foreground transition-colors">
            Home
          </a>
          <a href="/#features" className="text-muted-foreground hover:text-foreground transition-colors">
            Features
          </a>
          <a href="/#performance" className="text-muted-foreground hover:text-foreground transition-colors">
            Performance
          </a>
          <a href="/#agent" className="text-muted-foreground hover:text-foreground transition-colors">
            AI Agent
          </a>
        </nav>

        <div className="flex items-center space-x-3">
          {user ? (
            <>
              <Button
                size="sm"
                onClick={onNavigateToDashboard}
                className="flex items-center space-x-2 hidden md:flex"
              >
                <LayoutDashboard className="h-4 w-4" />
                <span>Dashboard</span>
              </Button>
              {/* User Menu */}
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="relative h-9 w-9 rounded-full">
                    <Avatar className="h-9 w-9">
                      <AvatarImage src={(user as any)?.avatar} alt={user.name} />
                      <AvatarFallback>
                        {getUserInitials(user.name)}
                      </AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-56" align="end" forceMount>
                  <div className="flex items-center justify-start gap-2 p-2">
                    <div className="flex flex-col space-y-1 leading-none">
                      <p className="font-medium">{user.name}</p>
                      {user.email && (
                        <p className="w-[200px] truncate text-sm text-muted-foreground">{user.email}</p>
                      )}
                    </div>
                  </div>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => (window.location.href = '/profile')}>
                    <UserIcon className="mr-2 h-4 w-4" />
                    <span>Profile</span>
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => logout?.()}>
                    <LogOut className="mr-2 h-4 w-4" />
                    <span>Log out</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </>
          ) : (
            <>
              <Button
                variant="ghost" 
                size="sm"
                onClick={handleSignIn}
                disabled={isLoading}
              >
                <LogIn className="h-4 w-4 mr-2" />
                Sign In
              </Button>
              <Button 
                size="sm"
                onClick={handleSignUp}
                disabled={isLoading}
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