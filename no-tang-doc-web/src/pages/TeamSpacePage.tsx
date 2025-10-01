import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { UserLayout } from '@/components/UserLayout';
import { SearchMode } from '@/components/SearchDialog';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Users, FileText, MessageSquare, Calendar, MoreVertical, ChevronDown, Settings } from 'lucide-react';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger, DropdownMenuSeparator } from '@/components/ui/dropdown-menu';

interface Team {
    id: string;
    name: string;
    description: string;
    avatar?: string;
    memberCount: number;
    documentCount: number;
}

interface TeamMember {
    id: string;
    name: string;
    email: string;
    role: 'owner' | 'admin' | 'member';
    avatar?: string;
    status: 'online' | 'offline' | 'away';
}

interface TeamDocument {
    id: string;
    name: string;
    type: string;
    uploadedBy: string;
    uploadDate: string;
    size: string;
}

interface TeamActivity {
    id: string;
    user: string;
    action: string;
    target: string;
    timestamp: string;
}

// Mock data
const mockTeams: Team[] = [
    {
        id: '1',
        name: 'Engineering Team',
        description: 'Core product development team',
        memberCount: 12,
        documentCount: 48,
    },
    {
        id: '2',
        name: 'Design Team',
        description: 'UI/UX and product design',
        memberCount: 8,
        documentCount: 32,
    },
    {
        id: '3',
        name: 'Marketing Team',
        description: 'Marketing and growth initiatives',
        memberCount: 6,
        documentCount: 24,
    },
];

const mockMembers: TeamMember[] = [
    {
        id: '1',
        name: 'John Doe',
        email: 'john@example.com',
        role: 'owner',
        status: 'online',
    },
    {
        id: '2',
        name: 'Jane Smith',
        email: 'jane@example.com',
        role: 'admin',
        status: 'online',
    },
    {
        id: '3',
        name: 'Bob Johnson',
        email: 'bob@example.com',
        role: 'member',
        status: 'away',
    },
    {
        id: '4',
        name: 'Alice Williams',
        email: 'alice@example.com',
        role: 'member',
        status: 'offline',
    },
];

const mockDocuments: TeamDocument[] = [
    {
        id: '1',
        name: 'Q1 Strategy.pdf',
        type: 'PDF',
        uploadedBy: 'John Doe',
        uploadDate: '2024-01-15',
        size: '2.4 MB',
    },
    {
        id: '2',
        name: 'Team Roadmap.xlsx',
        type: 'Excel',
        uploadedBy: 'Jane Smith',
        uploadDate: '2024-01-14',
        size: '156 KB',
    },
    {
        id: '3',
        name: 'Meeting Notes.docx',
        type: 'Word',
        uploadedBy: 'Bob Johnson',
        uploadDate: '2024-01-13',
        size: '89 KB',
    },
];

const mockActivities: TeamActivity[] = [
    {
        id: '1',
        user: 'Jane Smith',
        action: 'uploaded',
        target: 'Team Roadmap.xlsx',
        timestamp: '2 hours ago',
    },
    {
        id: '2',
        user: 'Bob Johnson',
        action: 'commented on',
        target: 'Q1 Strategy.pdf',
        timestamp: '4 hours ago',
    },
    {
        id: '3',
        user: 'Alice Williams',
        action: 'joined the team',
        target: '',
        timestamp: '1 day ago',
    },
];

export function TeamSpacePage() {
    const navigate = useNavigate();
    const [searchQuery, setSearchQuery] = useState('');
    const [searchMode, setSearchMode] = useState<SearchMode>('simple');
    const [selectedTeamId, setSelectedTeamId] = useState('1');

    const selectedTeam = mockTeams.find(team => team.id === selectedTeamId) || mockTeams[0];

    const handleSearch = (query: string, mode: SearchMode) => {
        setSearchQuery(query);
        setSearchMode(mode);
        navigate(`/documents?q=${encodeURIComponent(query)}&mode=${mode}`);
    };

    const getInitials = (name: string) => {
        return name
            .split(' ')
            .map((word) => word[0])
            .join('')
            .toUpperCase()
            .slice(0, 2);
    };

    const getStatusColor = (status: TeamMember['status']) => {
        switch (status) {
            case 'online':
                return 'bg-green-500';
            case 'away':
                return 'bg-yellow-500';
            case 'offline':
                return 'bg-gray-400';
            default:
                return 'bg-gray-400';
        }
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
                        <h1>TeamSpace</h1>
                        <p className="text-muted-foreground">
                            Collaborative workspace for your team
                        </p>
                    </div>
                    <div className="flex items-center gap-2">
                        <Button variant="outline">
                            <Calendar className="w-4 h-4 mr-2" />
                            Schedule Meeting
                        </Button>
                        <Button>
                            <MessageSquare className="w-4 h-4 mr-2" />
                            New Discussion
                        </Button>
                    </div>
                </div>

                {/* Team Info Card */}
                <Card>
                    <CardHeader>
                        <div className="flex items-start justify-between gap-4">
                            <div className="flex items-center gap-4 flex-1 min-w-0">
                                <Avatar className="h-16 w-16 flex-shrink-0">
                                    <AvatarFallback className="bg-primary/10 text-xl">
                                        {getInitials(selectedTeam.name)}
                                    </AvatarFallback>
                                </Avatar>
                                <div className="flex-1 min-w-0">
                                    <CardTitle className="mb-1">{selectedTeam.name}</CardTitle>
                                    <CardDescription className="mb-3">
                                        {selectedTeam.description}
                                    </CardDescription>
                                    <div className="flex items-center gap-4">
                                        <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
                                            <Users className="w-4 h-4 flex-shrink-0" />
                                            <span>{selectedTeam.memberCount} members</span>
                                        </div>
                                        <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
                                            <FileText className="w-4 h-4 flex-shrink-0" />
                                            <span>{selectedTeam.documentCount} documents</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div className="flex items-center gap-2 flex-shrink-0">
                                <Select value={selectedTeamId} onValueChange={setSelectedTeamId}>
                                    <SelectTrigger className="w-[200px]">
                                        <SelectValue placeholder="Switch team" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {mockTeams.map((team) => (
                                            <SelectItem key={team.id} value={team.id}>
                                                <div className="flex items-center gap-2">
                                                    <Avatar className="h-6 w-6">
                                                        <AvatarFallback className="text-xs">
                                                            {getInitials(team.name)}
                                                        </AvatarFallback>
                                                    </Avatar>
                                                    <span>{team.name}</span>
                                                </div>
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                                <DropdownMenu>
                                    <DropdownMenuTrigger asChild>
                                        <Button variant="outline" size="icon">
                                            <Settings className="w-4 h-4" />
                                        </Button>
                                    </DropdownMenuTrigger>
                                    <DropdownMenuContent align="end">
                                        <DropdownMenuItem>
                                            <Settings className="w-4 h-4 mr-2" />
                                            Team Settings
                                        </DropdownMenuItem>
                                        <DropdownMenuItem>
                                            <Users className="w-4 h-4 mr-2" />
                                            Manage Members
                                        </DropdownMenuItem>
                                        <DropdownMenuSeparator />
                                        <DropdownMenuItem className="text-destructive">
                                            Leave Team
                                        </DropdownMenuItem>
                                    </DropdownMenuContent>
                                </DropdownMenu>
                            </div>
                        </div>
                    </CardHeader>
                </Card>

                {/* Tabs */}
                <Tabs defaultValue="overview" className="space-y-4">
                    <TabsList>
                        <TabsTrigger value="overview">Overview</TabsTrigger>
                        <TabsTrigger value="members">Members</TabsTrigger>
                        <TabsTrigger value="documents">Documents</TabsTrigger>
                        <TabsTrigger value="activity">Activity</TabsTrigger>
                    </TabsList>

                    {/* Overview Tab */}
                    <TabsContent value="overview" className="space-y-4">
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                            {/* Recent Documents */}
                            <Card>
                                <CardHeader>
                                    <CardTitle className="text-base">Recent Documents</CardTitle>
                                </CardHeader>
                                <CardContent className="space-y-3">
                                    {mockDocuments.slice(0, 3).map((doc) => (
                                        <div key={doc.id} className="flex items-center justify-between">
                                            <div className="flex items-center gap-3">
                                                <div className="w-8 h-8 rounded bg-primary/10 flex items-center justify-center">
                                                    <FileText className="w-4 h-4" />
                                                </div>
                                                <div>
                                                    <p className="text-sm font-medium">{doc.name}</p>
                                                    <p className="text-xs text-muted-foreground">
                                                        {doc.uploadedBy} • {doc.uploadDate}
                                                    </p>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </CardContent>
                            </Card>

                            {/* Recent Activity */}
                            <Card>
                                <CardHeader>
                                    <CardTitle className="text-base">Recent Activity</CardTitle>
                                </CardHeader>
                                <CardContent className="space-y-3">
                                    {mockActivities.map((activity) => (
                                        <div key={activity.id} className="flex items-start gap-3">
                                            <Avatar className="h-8 w-8">
                                                <AvatarFallback className="text-xs">
                                                    {getInitials(activity.user)}
                                                </AvatarFallback>
                                            </Avatar>
                                            <div className="flex-1 min-w-0">
                                                <p className="text-sm">
                                                    <span className="font-medium">{activity.user}</span>{' '}
                                                    {activity.action}{' '}
                                                    {activity.target && (
                                                        <span className="font-medium">{activity.target}</span>
                                                    )}
                                                </p>
                                                <p className="text-xs text-muted-foreground">
                                                    {activity.timestamp}
                                                </p>
                                            </div>
                                        </div>
                                    ))}
                                </CardContent>
                            </Card>
                        </div>
                    </TabsContent>

                    {/* Members Tab */}
                    <TabsContent value="members" className="space-y-4">
                        <Card>
                            <CardHeader>
                                <div className="flex items-center justify-between">
                                    <CardTitle className="text-base">Team Members</CardTitle>
                                    <Button size="sm">Invite Member</Button>
                                </div>
                            </CardHeader>
                            <CardContent>
                                <div className="space-y-4">
                                    {mockMembers.map((member) => (
                                        <div key={member.id} className="flex items-center justify-between">
                                            <div className="flex items-center gap-3">
                                                <div className="relative">
                                                    <Avatar className="h-10 w-10">
                                                        <AvatarImage src={member.avatar} alt={member.name} />
                                                        <AvatarFallback>{getInitials(member.name)}</AvatarFallback>
                                                    </Avatar>

                                                </div>
                                                <div>
                                                    <p className="font-medium">{member.name}</p>
                                                    <p className="text-sm text-muted-foreground">{member.email}</p>
                                                </div>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <Badge variant={member.role === 'owner' ? 'default' : 'secondary'}>
                                                    {member.role}
                                                </Badge>
                                                <DropdownMenu>
                                                    <DropdownMenuTrigger asChild>
                                                        <Button variant="ghost" size="icon">
                                                            <MoreVertical className="w-4 h-4" />
                                                        </Button>
                                                    </DropdownMenuTrigger>
                                                    <DropdownMenuContent align="end">
                                                        <DropdownMenuItem>View Profile</DropdownMenuItem>
                                                        <DropdownMenuItem>Send Message</DropdownMenuItem>
                                                        {member.role !== 'owner' && (
                                                            <>
                                                                <Separator className="my-1" />
                                                                <DropdownMenuItem className="text-destructive">
                                                                    Remove from Team
                                                                </DropdownMenuItem>
                                                            </>
                                                        )}
                                                    </DropdownMenuContent>
                                                </DropdownMenu>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </CardContent>
                        </Card>
                    </TabsContent>

                    {/* Documents Tab */}
                    <TabsContent value="documents" className="space-y-4">
                        <Card>
                            <CardHeader>
                                <div className="flex items-center justify-between">
                                    <CardTitle className="text-base">Team Documents</CardTitle>
                                    <Button size="sm">Upload Document</Button>
                                </div>
                            </CardHeader>
                            <CardContent>
                                <div className="space-y-3">
                                    {mockDocuments.map((doc) => (
                                        <div
                                            key={doc.id}
                                            className="flex items-center justify-between p-3 rounded-lg border hover:bg-accent transition-colors"
                                        >
                                            <div className="flex items-center gap-3">
                                                <div className="w-10 h-10 rounded bg-primary/10 flex items-center justify-center">
                                                    <FileText className="w-5 h-5" />
                                                </div>
                                                <div>
                                                    <p className="font-medium">{doc.name}</p>
                                                    <p className="text-sm text-muted-foreground">
                                                        {doc.uploadedBy} • {doc.uploadDate} • {doc.size}
                                                    </p>
                                                </div>
                                            </div>
                                            <Button variant="outline" size="sm">
                                                View
                                            </Button>
                                        </div>
                                    ))}
                                </div>
                            </CardContent>
                        </Card>
                    </TabsContent>

                    {/* Activity Tab */}
                    <TabsContent value="activity" className="space-y-4">
                        <Card>
                            <CardHeader>
                                <CardTitle className="text-base">Team Activity</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="space-y-4">
                                    {mockActivities.map((activity) => (
                                        <div key={activity.id} className="flex items-start gap-3 pb-4 border-b last:border-0">
                                            <Avatar className="h-10 w-10">
                                                <AvatarFallback>{getInitials(activity.user)}</AvatarFallback>
                                            </Avatar>
                                            <div className="flex-1 min-w-0">
                                                <p>
                                                    <span className="font-medium">{activity.user}</span>{' '}
                                                    {activity.action}{' '}
                                                    {activity.target && (
                                                        <span className="font-medium">{activity.target}</span>
                                                    )}
                                                </p>
                                                <p className="text-sm text-muted-foreground">
                                                    {activity.timestamp}
                                                </p>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </CardContent>
                        </Card>
                    </TabsContent>
                </Tabs>
            </div>
        </UserLayout>
    );
}