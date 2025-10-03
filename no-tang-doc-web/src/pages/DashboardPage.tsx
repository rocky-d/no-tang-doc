import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../components/AuthContext';
import { Dashboard } from '../components/Dashboard';
import { UserLayout } from '../components/UserLayout';
import { SearchMode } from '../components/SearchDialog';

export function DashboardPage() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [searchQuery, setSearchQuery] = useState('');
    const [searchMode, setSearchMode] = useState<'simple' | 'advanced'>('simple');

    const handleSearch = (query: string, mode: SearchMode) => {
        setSearchQuery(query);
        setSearchMode(mode);
        // Navigate to documents page with search
        navigate(`/documents?q=${encodeURIComponent(query)}&mode=${mode}`);
    };

    return (
        <UserLayout
            onSearch={handleSearch}
            currentSearchQuery={searchQuery}
            currentSearchMode={searchMode}
        >
            <Dashboard />
        </UserLayout>
    );
}