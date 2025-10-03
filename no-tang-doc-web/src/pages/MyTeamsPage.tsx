import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { UserLayout } from '@/components/UserLayout';
import { SearchMode } from '@/components/SearchDialog';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Users, Plus, Settings, Crown } from 'lucide-react';

interface Team {
    id: string;
    name: string;
    description: string;
    memberCount: number;
    role: 'owner' | 'admin' | 'member';
    avatar?: string;
}

// Mock teams data
const mockTeams: Team[] = [
    {
        id: '1',
        name: 'Engineering Team',
        description: 'Core product development team',
        memberCount: 12,
        role: 'owner',
    },
    {
        id: '2',
        name: 'Design Team',
        description: 'UI/UX and product design',
        memberCount: 8,
        role: 'admin',
    },
    {
        id: '3',
        name: 'Marketing Team',
        description: 'Marketing and growth initiatives',
        memberCount: 6,
        role: 'member',
    },
];

export function MyTeamsPage() {
    const navigate = useNavigate();
    const [searchQuery, setSearchQuery] = useState('');
    const [searchMode, setSearchMode] = useState<SearchMode>('simple');
    const [teams] = useState<Team[]>(mockTeams);

    const handleSearch = (query: string, mode: SearchMode) => {
        setSearchQuery(query);
        setSearchMode(mode);
        navigate(`/documents?q=${encodeURIComponent(query)}&mode=${mode}`);
    };

    const getRoleBadge = (role: Team['role']) => {
        switch (role) {
            case 'owner':
                return <Badge className="bg-yellow-500/10 text-yellow-700 dark:text-yellow-400 border-yellow-500/20"><Crown className="w-3 h-3 mr-1" />Owner</Badge>;
            case 'admin':
                return <Badge variant="secondary">Admin</Badge>;
            case 'member':
                return <Badge variant="outline">Member</Badge>;
            default:
                return null;
        }
    };

    const getInitials = (name: string) => {
        return name
            .split(' ')
            .map((word) => word[0])
            .join('')
            .toUpperCase()
            .slice(0, 2);
    };

    return (
        <UserLayout
            onSearch={handleSearch}
            currentSearchQuery={searchQuery}
            currentSearchMode={searchMode}
        >
            <div className="space-y-6">
                {/* Header */}
                <div className="flex items-center justify-between">
                    <div>
                        <h1>My Teams</h1>
                        <p className="text-muted-foreground">
                            Manage and collaborate with your teams
                        </p>
                    </div>
                    <Button className="flex items-center gap-2">
                        <Plus className="w-4 h-4" />
                        Create Team
                    </Button>
                </div>

                {/* Teams Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {teams.map((team) => (
                        <Card key={team.id} className="hover:shadow-md transition-shadow cursor-pointer flex flex-col h-full">
                            <CardHeader>
                                <div className="flex items-start justify-between">
                                    <div className="flex items-center gap-3">
                                        <Avatar className="h-12 w-12">
                                            <AvatarImage src={team.avatar} alt={team.name} />
                                            <AvatarFallback className="bg-primary/10">
                                                {getInitials(team.name)}
                                            </AvatarFallback>
                                        </Avatar>
                                        <div>
                                            <CardTitle className="text-base">{team.name}</CardTitle>
                                            <div className="flex items-center gap-2 mt-1">
                                                <Users className="w-3 h-3 text-muted-foreground" />
                                                <span className="text-sm text-muted-foreground">
                          {team.memberCount} members
                        </span>
                                            </div>
                                        </div>
                                    </div>
                                    {(team.role === 'owner' || team.role === 'admin') && (
                                        <Button variant="ghost" size="icon" className="h-8 w-8">
                                            <Settings className="w-4 h-4" />
                                        </Button>
                                    )}
                                </div>
                            </CardHeader>
                            <CardContent className="flex flex-col flex-1">
                                <CardDescription className="mb-3 line-clamp-2" title={team.description}>
                                    {team.description}
                                </CardDescription>
                                <div className="flex items-center justify-between mt-auto">
                                    {getRoleBadge(team.role)}
                                    <Button variant="outline" size="sm">
                                        View Team
                                    </Button>
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>

                {/* Empty State */}
                {teams.length === 0 && (
                    <Card className="p-12">
                        <div className="flex flex-col items-center justify-center text-center">
                            <div className="rounded-full bg-muted p-6 mb-4">
                                <Users className="w-12 h-12 text-muted-foreground" />
                            </div>
                            <h3 className="mb-2">No teams yet</h3>
                            <p className="text-muted-foreground mb-4 max-w-md">
                                Create your first team to start collaborating with others on documents and projects.
                            </p>
                            <Button className="flex items-center gap-2">
                                <Plus className="w-4 h-4" />
                                Create Your First Team
                            </Button>
                        </div>
                    </Card>
                )}
            </div>
        </UserLayout>
    );
}