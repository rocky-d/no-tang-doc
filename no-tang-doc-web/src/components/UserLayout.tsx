import React, { useState } from 'react';
import { Search, Upload, User, LogOut, BarChart3, FileText, Settings, Moon, Sun, ChevronRight, Tag, X, Sparkles } from 'lucide-react';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from './ui/avatar';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger } from './ui/dropdown-menu';
import { Sidebar, SidebarContent, SidebarFooter, SidebarHeader, SidebarMenu, SidebarMenuButton, SidebarMenuItem, SidebarMenuSub, SidebarMenuSubButton, SidebarMenuSubItem, SidebarProvider, SidebarTrigger } from './ui/sidebar';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from './ui/collapsible';
import { Switch } from './ui/switch';
import { useAuth } from './AuthContext';
import { useTheme } from './ThemeContext';
import { SearchDialog, SearchMode } from './SearchDialog';

interface UserLayoutProps {
  children: React.ReactNode;
  onUploadClick: () => void;
  onNavigateHome?: () => void;
  activeView?: 'dashboard' | 'documents' | 'manage' | 'profile';
  onViewChange?: (view: 'dashboard' | 'documents' | 'manage' | 'profile') => void;
  onSearch?: (query: string, mode: SearchMode) => void;
  currentSearchQuery?: string;
  currentSearchMode?: SearchMode;
}

export function UserLayout({ 
  children, 
  onUploadClick, 
  onNavigateHome,
  activeView = 'dashboard',
  onViewChange,
  onSearch,
  currentSearchQuery = '',
  currentSearchMode = 'simple'
}: UserLayoutProps) {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [isManageOpen, setIsManageOpen] = useState(activeView === 'manage');
  const [searchDialogOpen, setSearchDialogOpen] = useState(false);

  const handleSearchClick = () => {
    // Only open dialog for documents and dashboard views
    if (activeView === 'documents' || activeView === 'dashboard') {
      setSearchDialogOpen(true);
    }
  };

  // Keyboard shortcut: Cmd/Ctrl + K to open search
  React.useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        if (activeView === 'documents' || activeView === 'dashboard') {
          setSearchDialogOpen(true);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [activeView]);

  const handleSearch = (query: string, mode: SearchMode) => {
    onSearch?.(query, mode);
  };

  const handleClearSearch = () => {
    onSearch?.('', 'simple');
  };

  const getSearchPlaceholder = () => {
    if (activeView === 'documents' || activeView === 'dashboard') {
      return 'Search documents...';
    }
    if (activeView === 'manage') {
      return 'Search tags...';
    }
    return 'Search...';
  };

  const isSearchActive = currentSearchQuery.trim().length > 0;

  const getUserInitials = (name: string) => {
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
  };

  const sidebarItems = [
    {
      title: 'Dashboard',
      icon: BarChart3,
      id: 'dashboard' as const,
      isActive: activeView === 'dashboard'
    },
    {
      title: 'Documents',
      icon: FileText,
      id: 'documents' as const,
      isActive: activeView === 'documents'
    },
    {
      title: 'Profile',
      icon: User,
      id: 'profile' as const,
      isActive: activeView === 'profile'
    }
  ];

  return (
    <SidebarProvider>
      <div className="min-h-screen flex w-full">
        {/* Sidebar */}
        <Sidebar>
          <SidebarHeader className="p-4">
            <button 
              className="flex items-center space-x-2 hover:opacity-80 transition-opacity"
              onClick={onNavigateHome}
            >
              <FileText className="h-6 w-6 text-primary" />
              <span className="font-semibold">DocRepo</span>
            </button>
          </SidebarHeader>
          <SidebarContent>
            <SidebarMenu>
              {sidebarItems.map((item) => (
                <SidebarMenuItem key={item.id}>
                  <SidebarMenuButton
                    isActive={item.isActive}
                    onClick={() => onViewChange?.(item.id)}
                  >
                    <item.icon className="h-4 w-4" />
                    <span>{item.title}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
              
              {/* Manage Menu with Submenu */}
              <Collapsible
                open={isManageOpen}
                onOpenChange={setIsManageOpen}
                className="group/collapsible"
              >
                <SidebarMenuItem>
                  <CollapsibleTrigger asChild>
                    <SidebarMenuButton
                      isActive={activeView === 'manage'}
                      className="w-full"
                    >
                      <Settings className="h-4 w-4" />
                      <span>Manage</span>
                      <ChevronRight className="ml-auto h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90" />
                    </SidebarMenuButton>
                  </CollapsibleTrigger>
                  <CollapsibleContent>
                    <SidebarMenuSub>
                      <SidebarMenuSubItem>
                        <SidebarMenuSubButton
                          onClick={() => onViewChange?.('manage')}
                          isActive={activeView === 'manage'}
                        >
                          <Tag className="h-4 w-4" />
                          <span>Tag</span>
                        </SidebarMenuSubButton>
                      </SidebarMenuSubItem>
                    </SidebarMenuSub>
                  </CollapsibleContent>
                </SidebarMenuItem>
              </Collapsible>
            </SidebarMenu>
          </SidebarContent>
          <SidebarFooter>
            <div className="flex items-center justify-between px-2 py-2">
              <div className="flex items-center gap-2">
                {theme === 'dark' ? (
                  <Moon className="h-4 w-4 text-sidebar-foreground" />
                ) : (
                  <Sun className="h-4 w-4 text-sidebar-foreground" />
                )}
                <span className="text-sm">Dark Mode</span>
              </div>
              <Switch
                checked={theme === 'dark'}
                onCheckedChange={toggleTheme}
              />
            </div>
          </SidebarFooter>
        </Sidebar>

        {/* Main Content */}
        <div className="flex-1 flex flex-col">
          {/* Top Navigation */}
          <header className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 sticky top-0 z-40">
            <div className="flex h-16 items-center gap-4 px-6">
              <SidebarTrigger />
              
              {/* Search Bar - always visible */}
              <div className="flex-1 max-w-2xl">
                <div className="flex items-center gap-2">
                  <div 
                    className="relative cursor-pointer flex-1"
                    onClick={handleSearchClick}
                  >
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground w-4 h-4 pointer-events-none" />
                    <div className={`pl-10 pr-20 h-10 flex items-center gap-2 border border-input rounded-md bg-background hover:bg-accent transition-colors ${isSearchActive ? 'border-primary' : ''}`}>
                      {isSearchActive && currentSearchMode === 'advanced' && (
                        <Badge variant="secondary" className="flex items-center gap-1 h-5">
                          <Sparkles className="w-3 h-3" />
                          AI
                        </Badge>
                      )}
                      <span className={isSearchActive ? 'text-foreground' : 'text-muted-foreground'}>
                        {currentSearchQuery || getSearchPlaceholder()}
                      </span>
                    </div>
                    {!isSearchActive && (activeView === 'documents' || activeView === 'dashboard') && (
                      <kbd className="absolute right-3 top-1/2 transform -translate-y-1/2 pointer-events-none hidden sm:inline-flex h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground opacity-100">
                        <span className="text-xs">⌘</span>K
                      </kbd>
                    )}
                  </div>
                  {isSearchActive && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={handleClearSearch}
                      className="h-10 px-3"
                    >
                      <X className="w-4 h-4" />
                    </Button>
                  )}
                </div>
              </div>

              {/* Search Dialog */}
              <SearchDialog
                open={searchDialogOpen}
                onOpenChange={setSearchDialogOpen}
                onSearch={handleSearch}
                initialMode={currentSearchMode}
                placeholder={getSearchPlaceholder()}
              />

              {/* Actions */}
              <div className="flex items-center gap-3">
                <Button onClick={onUploadClick} className="flex items-center gap-2">
                  <Upload className="w-4 h-4" />
                  Upload
                </Button>

                {/* User Menu */}
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" className="relative h-9 w-9 rounded-full">
                      <Avatar className="h-9 w-9">
                        <AvatarImage src={user?.avatar} alt={user?.name} />
                        <AvatarFallback>
                          {user?.name ? getUserInitials(user.name) : 'U'}
                        </AvatarFallback>
                      </Avatar>
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent className="w-56" align="end" forceMount>
                    <div className="flex items-center justify-start gap-2 p-2">
                      <div className="flex flex-col space-y-1 leading-none">
                        <p className="font-medium">{user?.name}</p>
                        <p className="w-[200px] truncate text-sm text-muted-foreground">
                          {user?.email}
                        </p>
                      </div>
                    </div>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem onClick={() => onViewChange?.('profile')}>
                      <User className="mr-2 h-4 w-4" />
                      <span>Profile</span>
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={logout}>
                      <LogOut className="mr-2 h-4 w-4" />
                      <span>Log out</span>
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            </div>
          </header>

          {/* Page Content */}
          <main className="flex-1 p-6">
            {children}
          </main>
        </div>
      </div>
    </SidebarProvider>
  );
}