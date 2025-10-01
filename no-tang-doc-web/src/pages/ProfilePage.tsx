import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { UserLayout } from '@/components/UserLayout';
import { Profile } from '@/components/Profile';
import { SearchMode } from '@/components/SearchDialog';

export function ProfilePage() {
    const navigate = useNavigate();
    const [searchQuery, setSearchQuery] = useState('');
    const [searchMode, setSearchMode] = useState<SearchMode>('simple');

    const handleSearch = (query: string, mode: SearchMode) => {
        setSearchQuery(query);
        setSearchMode(mode);
        navigate(`/documents?q=${encodeURIComponent(query)}&mode=${mode}`);
    };

    return (
        <UserLayout
            onSearch={handleSearch}
            currentSearchQuery={searchQuery}
            currentSearchMode={searchMode}
        >
            <Profile />
        </UserLayout>
    );
}