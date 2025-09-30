import React, { useState, useEffect } from 'react';
import { UserLayout } from './UserLayout';
import { SearchMode } from './SearchDialog';
import { DashboardOverview } from './DashboardOverview';
import { DocumentsList } from './DocumentsList';
import { TagManagement } from './TagManagement';
import { Profile } from './Profile';
// import { advancedSearch } from '../utils/documentApi'; // Uncomment to use real API

interface Document {
  id: string;
  name: string;
  type: string;
  size: string;
  uploadDate: string;
  category: string;
  tags: string[];
}

// Mock documents data
const mockDocuments: Document[] = [
  {
    id: '1',
    name: 'Project Proposal.pdf',
    type: 'PDF',
    size: '2.4 MB',
    uploadDate: '2024-01-15',
    category: 'Work',
    tags: ['Proposal', 'Q1 2024', 'Client', 'Important']
  },
  {
    id: '2',
    name: 'Invoice_2024_001.xlsx',
    type: 'Excel',
    size: '156 KB',
    uploadDate: '2024-01-14',
    category: 'Finance',
    tags: ['Invoice', 'Tax', 'Billing']
  },
  {
    id: '3',
    name: 'Meeting Notes.docx',
    type: 'Word',
    size: '89 KB',
    uploadDate: '2024-01-13',
    category: 'Work',
    tags: ['Meeting', 'Team', 'Action Items']
  },
  {
    id: '4',
    name: 'Design Mockups.fig',
    type: 'Figma',
    size: '5.2 MB',
    uploadDate: '2024-01-12',
    category: 'Design',
    tags: ['UI/UX', 'Mockup', 'Review', 'Version 2', 'Draft']
  },
  {
    id: '5',
    name: 'Tax Documents 2023.pdf',
    type: 'PDF',
    size: '1.8 MB',
    uploadDate: '2024-01-11',
    category: 'Finance',
    tags: ['Tax', 'Annual', '2023', 'Important', 'Archive']
  }
];

interface DashboardProps {
  onNavigateHome?: () => void;
  onNavigateToUpload?: () => void;
}

// Mock API function for advanced search
// To use your real backend API:
// 1. Uncomment the import of advancedSearch from '../utils/documentApi'
// 2. Replace mockAdvancedSearch with advancedSearch in the useEffect below
// 3. Update the API endpoint in /utils/documentApi.ts
const mockAdvancedSearch = async (query: string): Promise<Document[]> => {
  // Simulate API delay
  await new Promise(resolve => setTimeout(resolve, 500));
  
  // Mock advanced search logic - in real implementation, this would call your backend API
  // For now, return filtered results based on a more sophisticated search
  if (!query.trim()) {
    return mockDocuments;
  }
  
  // Simulate AI-powered search that understands context
  const lowerQuery = query.toLowerCase();
  return mockDocuments.filter(doc => {
    // Check if query matches document properties
    const nameMatch = doc.name.toLowerCase().includes(lowerQuery);
    const typeMatch = doc.type.toLowerCase().includes(lowerQuery);
    const categoryMatch = doc.category.toLowerCase().includes(lowerQuery);
    const tagMatch = doc.tags.some(tag => tag.toLowerCase().includes(lowerQuery));
    
    // Advanced: match partial words, synonyms, etc.
    const yearMatch = lowerQuery.includes('2024') || lowerQuery.includes('2023');
    const hasYear = doc.uploadDate.includes('2024') || doc.uploadDate.includes('2023');
    
    return nameMatch || typeMatch || categoryMatch || tagMatch || (yearMatch && hasYear);
  });
};

export function Dashboard({ onNavigateHome, onNavigateToUpload }: DashboardProps) {
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
    <UserLayout
      onUploadClick={onNavigateToUpload || (() => {})}
      onNavigateHome={onNavigateHome}
      activeView={activeView}
      onViewChange={setActiveView}
      onSearch={handleSearch}
      currentSearchQuery={searchQuery}
      currentSearchMode={searchMode}
    >
      {renderContent()}
    </UserLayout>
  );
}