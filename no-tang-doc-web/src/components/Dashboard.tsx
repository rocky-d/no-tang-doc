import { useState } from 'react';
import { UserLayout } from './UserLayout';
import type { SearchMode } from './SearchDialog';
import { DashboardOverview } from './DashboardOverview';
import { DocumentsList } from './DocumentsList';
import { TagManagement } from './TagManagement';
import { Profile } from './Profile';
// import { advancedSearch } from '../utils/documentApi'; // Uncomment to use real API
import type { Document } from '../mock-data/mock-data';
import { mockDocuments, mockAdvancedSearch } from '../mock-data/mock-data';

interface DashboardProps {
  onNavigateHome?: () => void;
  onNavigateToUpload?: () => void;
}

export function Dashboard({ onNavigateHome, onNavigateToUpload }: Readonly<DashboardProps>) {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchMode, setSearchMode] = useState<SearchMode>('simple');
  const [activeView, setActiveView] = useState<'dashboard' | 'documents' | 'manage' | 'profile'>('dashboard');
  const [documents] = useState<Document[]>(mockDocuments);
  const [searchResults, setSearchResults] = useState<Document[]>(mockDocuments);
  const [isSearching, setIsSearching] = useState(false);

  // Handle search from SearchDialog
  const handleSearch = async (query: string, mode: SearchMode) => {
    setSearchQuery(query);
    setSearchMode(mode);

    if (mode === 'advanced') {
      setIsSearching(true);
      try {
        const results = await mockAdvancedSearch(query);
        setSearchResults(results);
      } catch (error) {
        console.error('Advanced search failed:', error);
        setSearchResults(mockDocuments);
      } finally {
        setIsSearching(false);
      }
    } else {
      // For simple search, results will be filtered in DocumentsList
      setSearchResults(mockDocuments);
    }
  };

  const renderContent = () => {
    switch (activeView) {
      case 'documents':
        return (
          <DocumentsList
            documents={searchMode === 'advanced' ? searchResults : documents}
            searchTerm={searchMode === 'simple' ? searchQuery : ''}
            searchMode={searchMode}
            isSearching={isSearching}
          />
        );
      case 'manage':
        return <TagManagement searchTerm="" />;
      case 'profile':
        return <Profile />;
      default:
        return <DashboardOverview documents={documents} />;
    }
  };

  return (

      <DashboardOverview documents={documents} />

  );
}