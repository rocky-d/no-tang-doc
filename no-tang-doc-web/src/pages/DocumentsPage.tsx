import React, {useState} from 'react';
import {SearchMode} from '../components/SearchDialog';
import {DocumentsList} from '../components/DocumentsList';
import {UserLayout} from "../components/UserLayout.tsx";

interface Document {
    id: string;
    name: string;
    type: string;
    size: string;
    uploadDate: string;
    category: string;
    tags: string[];
}

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

const mockAdvancedSearch = async (query: string): Promise<Document[]> => {
    await new Promise(r => setTimeout(r, 400));
    if (!query.trim()) return mockDocuments;
    const q = query.toLowerCase();
    return mockDocuments.filter(d => {
        const nameMatch = d.name.toLowerCase().includes(q);
        const typeMatch = d.type.toLowerCase().includes(q);
        const categoryMatch = d.category.toLowerCase().includes(q);
        const tagMatch = d.tags.some(t => t.toLowerCase().includes(q));
        const yearMatch = q.includes('2024') || q.includes('2023');
        const hasYear = d.uploadDate.includes('2024') || d.uploadDate.includes('2023');
        return nameMatch || typeMatch || categoryMatch || tagMatch || (yearMatch && hasYear);
    });
};

export function DocumentsPage() {
    const [searchMode, setSearchMode] = useState<SearchMode>('simple');
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState<Document[]>(mockDocuments);
    const [isSearching, setIsSearching] = useState(false);

    const handleSearch = async (query: string, mode: SearchMode) => {
        setSearchQuery(query);
        setSearchMode(mode);
        if (mode === 'advanced') {
            setIsSearching(true);
            try {
                const res = await mockAdvancedSearch(query);
                setSearchResults(res);
            } finally {
                setIsSearching(false);
            }
        } else {
            setSearchResults(mockDocuments);
        }
    };

    return (
        <UserLayout
            onSearch={handleSearch}
            currentSearchQuery={searchQuery}
            currentSearchMode={searchMode}
        >
            <DocumentsList
                documents={searchMode === 'advanced' ? searchResults : mockDocuments}
                searchTerm={searchMode === 'simple' ? searchQuery : ''}
                searchMode={searchMode}
                isSearching={isSearching}
            />
        </UserLayout>

    );
}