import React, {useEffect, useState} from 'react';
import {SearchMode} from '../components/SearchDialog';
import {DocumentsList} from '../components/DocumentsList';
import {UserLayout} from "../components/UserLayout.tsx";
import { advancedSearch, getAllDocuments } from '../utils/documentApi';

interface Document {
    id: string;
    name: string;
    type: string;
    size: string;
    uploadDate: string;
    category: string;
    tags: string[];
}

export function DocumentsPage() {
    const [searchMode, setSearchMode] = useState<SearchMode>('simple');
    const [searchQuery, setSearchQuery] = useState('');
    const [allDocuments, setAllDocuments] = useState<Document[]>([]);
    const [searchResults, setSearchResults] = useState<Document[]>([]);
    const [isSearching, setIsSearching] = useState(false);

    // Initial load
    useEffect(() => {
        let mounted = true;
        (async () => {
            setIsSearching(true);
            try {
                const docs = await getAllDocuments();
                if (!mounted) return;
                setAllDocuments(docs);
                setSearchResults(docs); // default
            } catch (e) {
                console.error('Failed to load documents', e);
                if (mounted) {
                    setAllDocuments([]);
                    setSearchResults([]);
                }
            } finally {
                if (mounted) setIsSearching(false);
            }
        })();
        return () => {
            mounted = false;
        };
    }, []);

    const handleSearch = async (query: string, mode: SearchMode) => {
        setSearchQuery(query);
        setSearchMode(mode);
        if (mode === 'advanced') {
            setIsSearching(true);
            try {
                const res = await advancedSearch(query);
                setSearchResults(res);
            } catch (e) {
                console.error('Advanced search failed', e);
                setSearchResults([]);
            } finally {
                setIsSearching(false);
            }
        } else {
            // Simple mode uses client-side filter inside DocumentsList, provide full dataset
            setSearchResults(allDocuments);
        }
    };

    return (
        <UserLayout
            onSearch={handleSearch}
            currentSearchQuery={searchQuery}
            currentSearchMode={searchMode}
        >
            <DocumentsList
                documents={searchMode === 'advanced' ? searchResults : allDocuments}
                searchTerm={searchMode === 'simple' ? searchQuery : ''}
                searchMode={searchMode}
                isSearching={isSearching}
            />
        </UserLayout>

    );
}