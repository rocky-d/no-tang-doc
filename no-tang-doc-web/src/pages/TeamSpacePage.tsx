import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { UserLayout } from '@/components/UserLayout';
import type { SearchMode } from '@/components/SearchDialog';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import {Users, MoreVertical, Settings, Mail} from 'lucide-react';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger, DropdownMenuSeparator } from '@/components/ui/dropdown-menu';
import { getTeamRepository, type Team, type TeamMember } from '@/repositories/TeamRepository';
import { toast } from 'sonner';
import {Dialog, DialogContent, DialogDescription, DialogTitle, DialogFooter, DialogHeader, DialogClose} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { useAuth } from '@/components/AuthContext';

export function TeamSpacePage() {
    const navigate = useNavigate();
    const { user } = useAuth();
    const [searchQuery, setSearchQuery] = useState('');
    const [searchMode, setSearchMode] = useState<SearchMode>('simple');
    const [inviteDialogOpen, setInviteDialogOpen] = useState(false);
    const [inviteEmail, setInviteEmail] = useState('');
    const [inviteRole, setInviteRole] = useState<'admin' | 'member'>('member');
    const [inviteSubmitting, setInviteSubmitting] = useState(false);
    const [removeMemberDialogOpen, setRemoveMemberDialogOpen] = useState(false);
    const [memberToRemove, setMemberToRemove] = useState<TeamMember | null>(null);
    const [removeSubmitting, setRemoveSubmitting] = useState(false);

    // Data states
    const [teams, setTeams] = useState<Team[]>([]);
    const [teamsLoading, setTeamsLoading] = useState(false);
    const [teamsError, setTeamsError] = useState('');

    const [selectedTeamId, setSelectedTeamId] = useState<string>('');

    const [members, setMembers] = useState<TeamMember[]>([]);
    const [membersLoading, setMembersLoading] = useState(false);
    const [membersError, setMembersError] = useState('');

    const isSelf = (member: TeamMember): boolean => {
        const emailMatch = user?.email && member.email && user.email.toLowerCase() === member.email.toLowerCase();
        const usernameMatch = (user as unknown)?.username && member.name === (user as unknown).username;
        const nameMatch = user?.name && member.name === user.name;
        return !!(emailMatch || usernameMatch || nameMatch);
    };

    const selectedTeam = teams.find(team => team.id === selectedTeamId) || (teams.length > 0 ? teams[0] : undefined);
    const currentUserRole = members.find(m => isSelf(m))?.role;

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

    // Fetch teams (similar to MyTeamsPage)
    const fetchTeams = async () => {
        try {
            setTeamsLoading(true);
            setTeamsError('');
            const repo = getTeamRepository();
            const mapped: Team[] = await repo.getTeams({ activeOnly: true });
            setTeams(mapped);
            // Ensure a valid selected team id
            if (mapped.length > 0) {
                setSelectedTeamId(prev => (prev && mapped.some(t => t.id === prev)) ? prev : mapped[0].id);
            } else {
                setSelectedTeamId('');
            }
        } catch (e: unknown) {
            console.error('Fetch teams failed', e);
            const msg = e?.message || 'Failed to load teams';
            setTeamsError(msg);
            toast.error(msg);
        } finally {
            setTeamsLoading(false);
        }
    };

    // Fetch members for a team
    const fetchMembers = async (teamId: string) => {
        if (!teamId) return;
        try {
            setMembersLoading(true);
            setMembersError('');
            const repo = getTeamRepository();
            const mapped: TeamMember[] = await repo.getMembers(teamId);
            setMembers(mapped);
        } catch (e: unknown) {
            console.error('Fetch team members failed', e);
            const msg = e?.message || 'Failed to load team members';
            setMembersError(msg);
            toast.error(msg);
            setMembers([]);
        } finally {
            setMembersLoading(false);
        }
    };

    const handleInviteMember = async () => {
        if (!selectedTeam) {
            toast.error('No team selected');
            return;
        }
        if (!inviteEmail) {
            toast.error('Please enter an email address');
            return;
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(inviteEmail)) {
            toast.error('Please enter a valid email address');
            return;
        }
        if (inviteSubmitting) return;
        try {
            setInviteSubmitting(true);
            const repo = getTeamRepository();
            const invited = await repo.inviteMember(selectedTeam.id, inviteEmail.trim(), inviteRole);
            toast.success('Invitation sent');
            if (invited) {
                setMembers(prev => prev.some(m => m.id === invited.id || m.email === invited.email) ? prev : [...prev, invited]);
                setTeams(prev => prev.map(t => t.id === selectedTeam.id ? { ...t, memberCount: (t.memberCount || 0) + 1 } : t));
            } else {
                fetchMembers(selectedTeam.id);
            }
            // Reset form
            setInviteEmail('');
            setInviteRole('member');
            setInviteDialogOpen(false);
        } catch (e: unknown) {
            console.error('Invite member failed', e);
            toast.error(e?.message || 'Invitation failed');
        } finally {
            setInviteSubmitting(false);
        }
    };

    const handleRemoveMember = async () => {
        if (!selectedTeam || !memberToRemove) {
            toast.error('Required information is missing.');
            return;
        }
        if (removeSubmitting) return;

        try {
            setRemoveSubmitting(true);
            const repo = getTeamRepository();
            const { success, message } = await repo.removeMember(selectedTeam.id, memberToRemove.id);
            if (success) {
                toast.success(`Removed ${memberToRemove.name} from the team.`);
                // Update members list
                setMembers(prev => prev.filter(m => m.id !== memberToRemove.id));
                // Update team member count
                setTeams(prev => prev.map(t => t.id === selectedTeam.id ? { ...t, memberCount: Math.max(0, (t.memberCount || 1) - 1) } : t));
            } else {
                const errMsg = message || 'Failed to remove member';
                toast.error(errMsg);
            }
        } catch (e: unknown) {
            console.error('Remove member failed', e);
            toast.error(e?.message || 'Failed to remove member');
        } finally {
            setRemoveSubmitting(false);
            setRemoveMemberDialogOpen(false);
            setMemberToRemove(null);
        }
    };

    useEffect(() => {
        fetchTeams();

    }, []);

    useEffect(() => {
        if (selectedTeam?.id) {
            fetchMembers(selectedTeam.id);
        } else {
            setMembers([]);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedTeamId, teams.length]);

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
                </div>

                {/* Loading and Empty States */}
                {teamsLoading && (
                    <Card>
                        <CardContent className="py-8 text-center text-muted-foreground">Loading teams...</CardContent>
                    </Card>
                )}
                {!teamsLoading && teamsError && (
                    <Card>
                        <CardContent className="py-6">
                            <p className="text-destructive mb-3">{teamsError}</p>
                            <Button variant="outline" onClick={fetchTeams}>Retry</Button>
                        </CardContent>
                    </Card>
                )}

                {/* Main content when we have teams */}
                {!teamsLoading && !teamsError && teams.length > 0 && selectedTeam && (
                    <>
                        {/* Team Info Card */}
                        <Card>
                            <CardHeader>
                                <div className="flex items-start justify-between gap-4">
                                    <div className="flex items-center gap-4 flex-1 min-w-0">
                                        <Avatar className="h-16 w-16 flex-shrink-0">
                                            <AvatarImage src={selectedTeam.avatar} alt={selectedTeam.name} />
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
                                                    <span>{selectedTeam.memberCount ?? members.length} members</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-2 flex-shrink-0">
                                        <Select value={selectedTeamId} onValueChange={setSelectedTeamId}>
                                            <SelectTrigger className="w-[240px]">
                                                <SelectValue placeholder="Switch team" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {teams.map((team) => (
                                                    <SelectItem key={team.id} value={team.id}>
                                                        <div className="flex items-center gap-2">
                                                            <Avatar className="h-6 w-6">
                                                                <AvatarImage src={team.avatar} alt={team.name} />
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
                        <Tabs defaultValue="members" className="space-y-4">
                            <TabsList>
                                <TabsTrigger value="members">Members</TabsTrigger>
                                {/* <TabsTrigger value="overview">Overview</TabsTrigger> */}
                            </TabsList>

                            {/* Members Tab */}
                            <TabsContent value="members" className="space-y-4">
                                <Card>
                                    <CardHeader>
                                        <div className="flex items-center justify-between">
                                            <CardTitle className="text-base">Team Members</CardTitle>
                                            <Button size="sm" onClick={() => setInviteDialogOpen(true)}>
                                                <Mail className="w-4 h-4 mr-2" />
                                                Invite Member
                                            </Button>
                                        </div>
                                    </CardHeader>
                                    <CardContent>
                                        {membersLoading && (
                                            <div className="py-8 text-center text-muted-foreground">Loading members...</div>
                                        )}
                                        {!membersLoading && membersError && (
                                            <div className="py-4">
                                                <p className="text-destructive mb-3">{membersError}</p>
                                                <Button variant="outline" size="sm" onClick={() => fetchMembers(selectedTeam.id)}>Retry</Button>
                                            </div>
                                        )}
                                        {!membersLoading && !membersError && (
                                            <div className="space-y-4">
                                                {members.length === 0 && (
                                                    <div className="py-6 text-center text-muted-foreground">No members yet</div>
                                                )}
                                                {members.map((member) => (
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
                                                            {/* 仅当不是当前用户（owner/admin 自己隐藏），且不是 owner 时才显示操作菜单 */}
                                                            {currentUserRole && ['owner', 'admin'].includes(currentUserRole) && !isSelf(member) && member.role !== 'owner' && (
                                                                <DropdownMenu>
                                                                    <DropdownMenuTrigger asChild>
                                                                        <Button variant="ghost" size="icon">
                                                                            <MoreVertical className="w-4 h-4" />
                                                                        </Button>
                                                                    </DropdownMenuTrigger>
                                                                    <DropdownMenuContent align="end">
                                                                        <DropdownMenuItem
                                                                            className="text-destructive"
                                                                            onClick={() => {
                                                                                setMemberToRemove(member);
                                                                                setRemoveMemberDialogOpen(true);
                                                                            }}
                                                                        >
                                                                            Remove from Team
                                                                        </DropdownMenuItem>
                                                                    </DropdownMenuContent>
                                                                </DropdownMenu>
                                                            )}
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </CardContent>
                                </Card>
                            </TabsContent>
                        </Tabs>
                    </>
                )}

                {/* Empty state when no teams */}
                {!teamsLoading && !teamsError && teams.length === 0 && (
                    <Card>
                        <CardContent className="py-12 text-center text-muted-foreground">No teams found</CardContent>
                    </Card>
                )}
                {/* Invite Member Dialog */}
                <Dialog open={inviteDialogOpen} onOpenChange={setInviteDialogOpen}>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>Invite Team Member</DialogTitle>
                            <DialogDescription>
                                Send an invitation to add a new member to your team.
                            </DialogDescription>
                        </DialogHeader>
                        <div className="space-y-4 py-4">
                            <div className="space-y-2">
                                <Label htmlFor="email">Email Address</Label>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="member@example.com"
                                    value={inviteEmail}
                                    onChange={(e) => setInviteEmail(e.target.value)}
                                    onKeyDown={(e) => {
                                        if (e.key === 'Enter') {
                                            handleInviteMember();
                                        }
                                    }}
                                />
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="role">Role</Label>
                                <Select value={inviteRole} onValueChange={(value) => setInviteRole(value as 'admin' | 'member')}>
                                    <SelectTrigger id="role">
                                        <SelectValue placeholder="Select role" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="member">Member</SelectItem>
                                        <SelectItem value="admin">Admin</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>
                        <DialogFooter>
                            <Button variant="outline" onClick={() => setInviteDialogOpen(false)}>
                                Cancel
                            </Button>
                            <Button onClick={handleInviteMember} disabled={inviteSubmitting}>
                                {inviteSubmitting ? 'Sending...' : 'Send Invitation'}
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>

                {/* Remove Member Confirmation Dialog */}
                <Dialog open={removeMemberDialogOpen} onOpenChange={setRemoveMemberDialogOpen}>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>Remove Team Member</DialogTitle>
                            <DialogDescription>
                                Are you sure you want to remove <span className="font-semibold">{memberToRemove?.name}</span> from the team? This action cannot be undone.
                            </DialogDescription>
                        </DialogHeader>
                        <DialogFooter>
                            <DialogClose asChild>
                                <Button variant="outline">Cancel</Button>
                            </DialogClose>
                            <Button variant="destructive" onClick={handleRemoveMember} disabled={removeSubmitting}>
                                {removeSubmitting ? 'Removing...' : 'Remove Member'}
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            </div>
        </UserLayout>
    );
}