import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { UserLayout } from '../components/UserLayout';
import type { SearchMode } from '../components/SearchDialog';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Badge } from '../components/ui/badge';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '../components/ui/dropdown-menu';
import { Separator } from '../components/ui/separator';
import { Avatar, AvatarFallback, AvatarImage } from '../components/ui/avatar';
import { Users, Plus, Settings, Crown, Loader2, Trash2 } from 'lucide-react';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '../components/ui/dialog';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Textarea } from '../components/ui/textarea';
import { toast } from 'sonner';
import { http } from '../utils/request';
import { useAuth } from '../components/AuthContext';

interface Team {
    id: string;
    name: string;
    description: string;
    memberCount: number;
    role: 'owner' | 'admin' | 'member';
    avatar?: string;
    ownerName?: string;
    ownerEmail?: string;
}

const TEAMS_API_PREFIX = (import.meta.env as any).VITE_TEAMS_API_PREFIX || 'https://api.ntdoc.site/api/v1/teams';

export function MyTeamsPage() {
    const navigate = useNavigate();
    const { user } = useAuth();
    const currentOwnerName = user?.username ?? user?.name ?? (user as any)?.id;
    const currentOwnerEmail = user?.email;

    const [searchQuery, setSearchQuery] = useState('');
    const [searchMode, setSearchMode] = useState<SearchMode>('simple');
    const [teams, setTeams] = useState<Team[]>([]);
    const [createDialogOpen, setCreateDialogOpen] = useState(false);
    const [teamName, setTeamName] = useState('');
    const [teamDescription, setTeamDescription] = useState('');
    const [creating, setCreating] = useState(false);
    const [teamsLoading, setTeamsLoading] = useState<boolean>(false);
    const [teamsError, setTeamsError] = useState<string>('');
    const [viewDialogOpen, setViewDialogOpen] = useState(false);
    const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);
    const [editTeamName, setEditTeamName] = useState('');
    const [editTeamDescription, setEditTeamDescription] = useState('');
    const [editSaving, setEditSaving] = useState(false);
    const [deletingId, setDeletingId] = useState<string | null>(null);

    // Load teams from backend
    const fetchTeams = async () => {
        try {
            setTeamsLoading(true);
            setTeamsError('');
            const resp: any = await http.get(`${TEAMS_API_PREFIX}?activeOnly=true`);
            const body = resp?.data ?? resp ?? {};
            const list = body?.data?.teams ?? body?.teams ?? [];
            const mapped: Team[] = (Array.isArray(list) ? list : []).map((t: any) => ({
                id: String(t?.teamId ?? t?.id ?? Math.random().toString(36).slice(2)),
                name: t?.name ?? 'Unnamed Team',
                description: t?.description ?? '',
                memberCount: Number(t?.memberCount ?? 0),
                role: 'owner',
                avatar: undefined,
                ownerName: currentOwnerName,
                ownerEmail: currentOwnerEmail,
            }));
            setTeams(mapped);
        } catch (e: any) {
            console.error('Fetch teams failed', e);
            setTeamsError(e?.message || '获取团队列表失败');
            toast.error(e?.message || '获取团队列表失败');
        } finally {
            setTeamsLoading(false);
        }
    };

    useEffect(() => {
        fetchTeams();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleSearch = (query: string, mode: SearchMode) => {
        setSearchQuery(query);
        setSearchMode(mode);
        navigate(`/documents?q=${encodeURIComponent(query)}&mode=${mode}`);
    };

    const handleCreateTeam = async () => {
        const name = teamName.trim();
        const description = teamDescription;

        if (!name) {
            toast.error('Name can not be blank');
            return;
        }
        if (name.length > 50) {
            toast.error('Name can not longer than 50 characters');
            return;
        }
        if (creating) return;

        try {
            setCreating(true);
            const payload = { name, description };
            const resp: any = await http.post(`${TEAMS_API_PREFIX}`, payload);
            const body = resp?.data ?? resp ?? {};
            const statusOk = typeof resp?.status === 'number' ? (resp.status >= 200 && resp.status < 300) : false;
            const successOk = resp?.success === true;
            const ok = statusOk || successOk;
            if (!ok) {
                throw new Error(body?.message || 'Create Team failed');
            }
            const data = body?.data ?? {};

            const newTeam: Team = {
                id: String(data?.teamId ?? data?.id ?? Math.random().toString(36).slice(2)),
                name: data?.name ?? name,
                description: data?.description ?? description,
                memberCount: Number(data?.memberCount ?? 1),
                role: 'owner',
                avatar: undefined,
                ownerName: currentOwnerName,
                ownerEmail: currentOwnerEmail,
            };

            setTeams(prev => [newTeam, ...prev]);
            toast.success(body?.message || 'Team created successfully');

            // Reset form and close dialog
            setTeamName('');
            setTeamDescription('');
            setCreateDialogOpen(false);
        } catch (e: any) {
            console.error('Create team failed', e);
            toast.error(e?.message || 'Create Team failed');
        } finally {
            setCreating(false);
        }
    };
    const handleViewTeam = (team: Team) => {
        setSelectedTeam(team);
        setEditTeamName(team.name);
        setEditTeamDescription(team.description);
        setViewDialogOpen(true);
    };
    const handleEditTeam = async () => {
        if (!selectedTeam) return;
        const newName = editTeamName.trim();
        const newDesc = editTeamDescription;
        if (!newName) {
            toast.error('Team name can not be blank');
            return;
        }
        if (newName.length > 50) {
            toast.error('Team name can not be longer than 50 characters');
            return;
        }
        if (editSaving) return;
        try {
            setEditSaving(true);
            const payload = { name: newName, description: newDesc };
            const resp: any = await http.put(`${TEAMS_API_PREFIX}/${selectedTeam.id}`, payload);
            const body = resp?.data ?? resp ?? {};
            const statusOk = typeof resp?.status === 'number' ? (resp.status >= 200 && resp.status < 300) : false;
            const successOk = resp?.success === true;
            const ok = statusOk || successOk;
            if (!ok) {
                throw new Error(body?.message || 'Update team failed');
            }
            const data = body?.data ?? {};
            const updated = {
                name: data?.name ?? newName,
                description: data?.description ?? newDesc,
                memberCount: Number(data?.memberCount ?? selectedTeam.memberCount),
            };
            // Update list and selectedTeam
            setTeams(prev => prev.map(t => t.id === selectedTeam.id ? { ...t, ...updated } : t));
            setSelectedTeam(prev => prev ? { ...prev, ...updated } : prev);

            toast.success(body?.message || 'Team updated successfully');
            setViewDialogOpen(false);
        } catch (e: any) {
            console.error('Update team failed', e);
            toast.error(e?.message || 'Update team failed');
        } finally {
            setEditSaving(false);
        }
    };
    const handleDeleteTeam = async (teamId: string) => {
        if (!teamId) return;
        const ok = window.confirm('确定要删除该团队吗？此操作不可撤销。');
        if (!ok) return;
        try {
            setDeletingId(teamId);
            const resp: any = await http.delete(`${TEAMS_API_PREFIX}/${teamId}`);
            const body = resp?.data ?? resp ?? {};
            const statusOk = typeof resp?.status === 'number' ? (resp.status >= 200 && resp.status < 300) : false;
            const successOk = resp?.success === true;
            const success = statusOk || successOk;
            if (!success) {
                throw new Error(body?.message || 'Delete team failed');
            }
            setTeams(prev => prev.filter(t => t.id !== teamId));
            // If currently viewing this team, close dialog
            setSelectedTeam(prev => (prev && prev.id === teamId) ? null : prev);
            if (viewDialogOpen && selectedTeam && selectedTeam.id === teamId) {
                setViewDialogOpen(false);
            }
            toast.success(body?.message || 'Team deleted successfully');
        } catch (e: any) {
            console.error('Delete team failed', e);
            toast.error(e?.message || 'Delete team failed');
        } finally {
            setDeletingId(null);
        }
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
                    <Button className="flex items-center gap-2" onClick={() => setCreateDialogOpen(true)}>
                        <Plus className="w-4 h-4" />
                        Create Team
                    </Button>
                </div>

                {/* Loading State */}
                {teamsLoading && (
                    <div className="flex items-center justify-center py-16">
                        <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
                    </div>
                )}

                {/* Error Retry (optional simple display) */}
                {!teamsLoading && teamsError && (
                    <Card className="p-6">
                        <p className="text-destructive mb-4">{teamsError}</p>
                        <Button variant="outline" onClick={fetchTeams}>重试</Button>
                    </Card>
                )}

                {/* Teams Grid */}
                {!teamsLoading && !teamsError && (
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
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button variant="ghost" size="icon" className="h-8 w-8">
                                                        <Settings className="w-4 h-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuItem
                                                        onClick={() => handleDeleteTeam(team.id)}
                                                        className="text-destructive"
                                                        disabled={deletingId === team.id}
                                                    >
                                                        {deletingId === team.id ? (
                                                            <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                                        ) : (
                                                            <Trash2 className="w-4 h-4 mr-2" />
                                                        )}
                                                        {deletingId === team.id ? 'Deleting...' : 'Delete'}
                                                    </DropdownMenuItem>
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        )}
                                    </div>
                                </CardHeader>
                                <CardContent className="flex flex-col flex-1">
                                    <CardDescription className="mb-3 line-clamp-2" title={team.description}>
                                        {team.description}
                                    </CardDescription>
                                    <div className="flex items-center justify-between mt-auto">
                                        {getRoleBadge(team.role)}
                                        <Button variant="outline" size="sm" onClick={() => handleViewTeam(team)}>
                                            View Team
                                        </Button>
                                    </div>
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                )}

                {/* Empty State */}
                {!teamsLoading && !teamsError && teams.length === 0 && (
                    <Card className="p-12">
                        <div className="flex flex-col items-center justify-center text-center">
                            <div className="rounded-full bg-muted p-6 mb-4">
                                <Users className="w-12 h-12 text-muted-foreground" />
                            </div>
                            <h3 className="mb-2">No teams yet</h3>
                            <p className="text-muted-foreground mb-4 max-w-md">
                                Create your first team to start collaborating with others on documents and projects.
                            </p>
                            <Button className="flex items-center gap-2" onClick={() => setCreateDialogOpen(true)}>
                                <Plus className="w-4 h-4" />
                                Create Your First Team
                            </Button>
                        </div>
                    </Card>
                )}
            </div>

            {/* Create Team Dialog */}
            <Dialog open={createDialogOpen} onOpenChange={(open: boolean) => { if (!creating) setCreateDialogOpen(open); }}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>Create Team</DialogTitle>
                        <DialogDescription>
                            Add a new team to manage and collaborate with others.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="name">Name</Label>
                            <Input
                                id="name"
                                placeholder="Team Name"
                                value={teamName}
                                onChange={(e) => setTeamName(e.target.value)}
                                maxLength={50}
                            />
                            <p className="text-xs text-muted-foreground">最多 50 个字符</p>
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="description">Description</Label>
                            <Textarea
                                id="description"
                                placeholder="Team Description"
                                value={teamDescription}
                                onChange={(e) => setTeamDescription(e.target.value)}
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button type="button" onClick={handleCreateTeam} disabled={creating || !teamName.trim() || teamName.trim().length > 50}>
                            {creating ? (<><Loader2 className="w-4 h-4 mr-2 animate-spin" />Creating...</>) : 'Create Team'}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
            {/* View Team Dialog */}
            <Dialog open={viewDialogOpen} onOpenChange={(open: boolean) => { if (!editSaving) setViewDialogOpen(open); }}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>Team Details</DialogTitle>
                        <DialogDescription>
                            View and edit team details.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4">
                        {/* 团队名称输入框 */}
                        <div className="space-y-2">
                            <Label htmlFor="name">Name</Label>
                            <Input
                                id="name"
                                placeholder="Team Name"
                                value={editTeamName}
                                onChange={(e) => setEditTeamName(e.target.value)}
                                maxLength={50}
                            />
                            <p className="text-xs text-muted-foreground">最多 50 个字符</p>
                        </div>

                        {/* 团队描述输入框 */}
                        <div className="space-y-2">
                            <Label htmlFor="description">Description</Label>
                            <Textarea
                                id="description"
                                placeholder="Team Description"
                                value={editTeamDescription}
                                onChange={(e) => setEditTeamDescription(e.target.value)}
                            />
                        </div>

                        {/* 分隔线 */}
                        <Separator />

                        {/* 团队所有者信息 */}
                        <div className="space-y-2">
                            <Label>Owner</Label>
                            <div className="flex items-center gap-3">
                                <Avatar className="h-12 w-12">
                                    <AvatarImage src={selectedTeam?.avatar} alt={selectedTeam?.name} />
                                    <AvatarFallback className="bg-primary/10">
                                        {getInitials(selectedTeam?.name || '')}
                                    </AvatarFallback>
                                </Avatar>
                                <div>
                                    <CardTitle className="text-base">{selectedTeam?.ownerName}</CardTitle>
                                    <div className="flex items-center gap-2 mt-1">
                                        <Users className="w-3 h-3 text-muted-foreground" />
                                        <span className="text-sm text-muted-foreground">
                {selectedTeam?.ownerEmail}
              </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Save 按钮 */}
                    <DialogFooter>
                        <Button type="button" onClick={handleEditTeam} disabled={editSaving || !editTeamName.trim() || editTeamName.trim().length > 50}>
                            {editSaving ? (<><Loader2 className="w-4 h-4 mr-2 animate-spin" />Saving...</>) : 'Save Changes'}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </UserLayout>
    );
}
