import React, { useState, useEffect } from 'react';
import { Search, Sparkles, Clock, X, ArrowRight } from 'lucide-react';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from './ui/dialog';
import { Input } from './ui/input';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { ScrollArea } from './ui/scroll-area';

export type SearchMode = 'simple' | 'advanced';

interface SearchHistoryItem {
  query: string;
  mode: SearchMode;
  timestamp: number;
}

interface SearchDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSearch: (query: string, mode: SearchMode) => void;
  initialMode?: SearchMode;
  placeholder?: string;
}

const STORAGE_KEY = 'doc-repo-search-history';
const MAX_HISTORY_ITEMS = 10;

export function SearchDialog({
  open,
  onOpenChange,
  onSearch,
  initialMode = 'simple',
  placeholder = 'Search documents...'
}: SearchDialogProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchMode, setSearchMode] = useState<SearchMode>(initialMode);
  const [searchHistory, setSearchHistory] = useState<SearchHistoryItem[]>([]);

  // Load search history from localStorage
  useEffect(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const history = JSON.parse(stored) as SearchHistoryItem[];
        setSearchHistory(history);
      }
    } catch (error) {
      console.error('Failed to load search history:', error);
    }
  }, []);

  // Save search history to localStorage
  const saveToHistory = (query: string, mode: SearchMode) => {
    if (!query.trim()) return;

    const newItem: SearchHistoryItem = {
      query: query.trim(),
      mode,
      timestamp: Date.now()
    };

    // Remove duplicate queries and add new item at the beginning
    const updatedHistory = [
      newItem,
      ...searchHistory.filter(item => item.query.toLowerCase() !== query.toLowerCase())
    ].slice(0, MAX_HISTORY_ITEMS);

    setSearchHistory(updatedHistory);
    
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(updatedHistory));
    } catch (error) {
      console.error('Failed to save search history:', error);
    }
  };

  const handleSearch = () => {
    if (searchQuery.trim()) {
      saveToHistory(searchQuery, searchMode);
      onSearch(searchQuery, searchMode);
      onOpenChange(false);
    }
  };

  const handleHistoryClick = (item: SearchHistoryItem) => {
    setSearchQuery(item.query);
    setSearchMode(item.mode);
    onSearch(item.query, item.mode);
    onOpenChange(false);
  };

  const clearHistory = () => {
    setSearchHistory([]);
    localStorage.removeItem(STORAGE_KEY);
  };

  const removeHistoryItem = (index: number) => {
    const updatedHistory = searchHistory.filter((_, i) => i !== index);
    setSearchHistory(updatedHistory);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updatedHistory));
  };

  const formatTimestamp = (timestamp: number) => {
    const now = Date.now();
    const diff = now - timestamp;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
    return new Date(timestamp).toLocaleDateString();
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  // Reset search query when dialog closes
  useEffect(() => {
    if (!open) {
      setSearchQuery('');
    }
  }, [open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl p-0 gap-0">
        <DialogHeader className="px-6 pt-6 pb-4">
          <DialogTitle>Search Documents</DialogTitle>
          <DialogDescription className="sr-only">
            Search through your documents using simple or advanced AI-powered search
          </DialogDescription>
        </DialogHeader>

        <Tabs value={searchMode} onValueChange={(value) => setSearchMode(value as SearchMode)} className="w-full">
          <div className="px-6 pb-4">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="simple" className="flex items-center gap-2">
                <Search className="w-4 h-4" />
                Simple Search
              </TabsTrigger>
              <TabsTrigger value="advanced" className="flex items-center gap-2">
                <Sparkles className="w-4 h-4" />
                Advanced Search
              </TabsTrigger>
            </TabsList>
          </div>

          <div className="px-6 pb-4">
            <TabsContent value="simple" className="mt-0">
              <div className="space-y-3">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground w-4 h-4" />
                  <Input
                    placeholder="Search by filename or tags..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyDown={handleKeyDown}
                    className="pl-10 pr-4"
                    autoFocus
                  />
                </div>
                <p className="text-sm text-muted-foreground">
                  Search through document names, types, categories, and tags. Results are instant.
                </p>
              </div>
            </TabsContent>

            <TabsContent value="advanced" className="mt-0">
              <div className="space-y-3">
                <div className="relative">
                  <Sparkles className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground w-4 h-4" />
                  <Input
                    placeholder="Search with AI-powered advanced search..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyDown={handleKeyDown}
                    className="pl-10 pr-4"
                    autoFocus
                  />
                </div>
                <p className="text-sm text-muted-foreground">
                  Use natural language to find documents. AI-powered search understands context and semantics.
                </p>
              </div>
            </TabsContent>
          </div>

          <div className="px-6 pb-6">
            <Button onClick={handleSearch} className="w-full" disabled={!searchQuery.trim()}>
              <Search className="w-4 h-4 mr-2" />
              Search
            </Button>
          </div>
        </Tabs>

        {/* Search History */}
        {searchHistory.length > 0 && (
          <div className="border-t">
            <div className="px-6 py-4">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <Clock className="w-4 h-4 text-muted-foreground" />
                  <h3 className="font-medium">Recent Searches</h3>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={clearHistory}
                  className="h-auto py-1 px-2"
                >
                  Clear all
                </Button>
              </div>

              <ScrollArea className="h-[200px]">
                <div className="space-y-2">
                  {searchHistory.map((item, index) => (
                    <div
                      key={index}
                      className="flex items-center justify-between gap-3 p-2 rounded-md hover:bg-accent group cursor-pointer"
                      onClick={() => handleHistoryClick(item)}
                    >
                      <div className="flex items-center gap-3 flex-1 min-w-0">
                        {item.mode === 'advanced' ? (
                          <Sparkles className="w-4 h-4 text-muted-foreground flex-shrink-0" />
                        ) : (
                          <Search className="w-4 h-4 text-muted-foreground flex-shrink-0" />
                        )}
                        <div className="flex-1 min-w-0">
                          <p className="text-sm truncate">{item.query}</p>
                          <div className="flex items-center gap-2 mt-0.5">
                            <Badge variant="secondary" className="text-xs h-5">
                              {item.mode}
                            </Badge>
                            <span className="text-xs text-muted-foreground">
                              {formatTimestamp(item.timestamp)}
                            </span>
                          </div>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <ArrowRight className="w-4 h-4 text-muted-foreground opacity-0 group-hover:opacity-100 transition-opacity" />
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-auto p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                          onClick={(e) => {
                            e.stopPropagation();
                            removeHistoryItem(index);
                          }}
                        >
                          <X className="w-3 h-3" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}