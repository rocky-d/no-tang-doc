// src/pages/DocumentsPage.tsx
import { useState } from 'react';
import type { SearchMode } from '../components/SearchDialog';
import { DocumentsList } from '../components/DocumentsList';

// 与 Dashboard 内部保持一致的模拟数据(可提取到独立模块)
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
    { id: '1', name: 'Project Proposal.pdf', type: 'PDF', size: '2.4 MB', uploadDate: '2024-01-15', category: 'Work', tags: ['Proposal','Q1 2024','Client','Important'] },
    { id: '2', name: 'Invoice_2024_001.xlsx', type: 'Excel', size: '156 KB', uploadDate: '2024-01-14', category: 'Finance', tags: ['Invoice','Tax','Billing'] },
    { id: '3', name: 'Meeting Notes.docx', type: 'Word', size: '89 KB', uploadDate: '2024-01-13', category: 'Work', tags: ['Meeting','Team','Action Items'] },
    { id: '4', name: 'Design Mockups.fig', type: 'Figma', size: '5.2 MB', uploadDate: '2024-01-12', category: 'Design', tags: ['UI/UX','Mockup','Review','Version 2','Draft'] },
    { id: '5', name: 'Tax Documents 2023.pdf', type: 'PDF', size: '1.8 MB', uploadDate: '2024-01-11', category: 'Finance', tags: ['Tax','Annual','2023','Important','Archive'] }
];

// 可替换为真实高级搜索
const mockAdvancedSearch = async (query: string): Promise<Document[]> => {
    await new Promise(r => setTimeout(r, 400));
    if (!query.trim()) return mockDocuments;
    const q = query.toLowerCase();
    return mockDocuments.filter(d => {
        const yearMatch = q.includes('2024') || q.includes('2023');
        const hasYear = d.uploadDate.includes('2024') || d.uploadDate.includes('2023');
        return (
            d.name.toLowerCase().includes(q) ||
            d.type.toLowerCase().includes(q) ||
            d.category.toLowerCase().includes(q) ||
            d.tags.some(t => t.toLowerCase().includes(q)) ||
            (yearMatch && hasYear)
        );
    });
};

export function DocumentsPage() {
    const [documents] = useState<Document[]>(mockDocuments);
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
        <div style={{ padding: 24 }}>
            {/* 这里可复用统一 SearchDialog 触发方式；临时简单输入框示例 */}
            <div style={{ marginBottom: 16, display: 'flex', gap: 8 }}>
                <input
                    placeholder="Search..."
                    value={searchQuery}
                    onChange={e => handleSearch(e.target.value, searchMode)}
                    style={{ flex: 1 }}
                />
                <select
                    value={searchMode}
                    onChange={e => handleSearch(searchQuery, e.target.value as SearchMode)}
                >
                    <option value="simple">Simple</option>
                    <option value="advanced">Advanced</option>
                </select>
            </div>
            <DocumentsList
                documents={searchMode === 'advanced' ? searchResults : documents}
                searchTerm={searchMode === 'simple' ? searchQuery : ''}
                searchMode={searchMode}
                isSearching={isSearching}
            />
        </div>
    );
}