// filepath: d:\Code\NoTangDoc\no-tang-doc-web\src\repositories\TeamRepository.ts
/**
 * Team Repository - abstracts Teams and Members related API calls.
 */
import { http } from '../utils/request';

export interface Team {
  id: string;
  name: string;
  description: string;
  memberCount: number;
  avatar?: string;
  documentCount?: number;
}

export type TeamRole = 'owner' | 'admin' | 'member';

export interface TeamMember {
  id: string;
  name: string;
  email: string;
  role: TeamRole;
  avatar?: string;
  status?: 'online' | 'offline' | 'away';
}

export interface ITeamRepository {
  getTeams(options?: { activeOnly?: boolean }): Promise<Team[]>;
  createTeam(input: { name: string; description?: string }): Promise<Team>;
  updateTeam(teamId: string, input: { name: string; description?: string }): Promise<Team>;
  deleteTeam(teamId: string): Promise<{ success: boolean; message?: string }>;

  getMembers(teamId: string): Promise<TeamMember[]>;
  inviteMember(teamId: string, userEmail: string, role: TeamRole): Promise<TeamMember | null>;
  removeMember(teamId: string, memberId: string): Promise<{ success: boolean; message?: string }>;
}

const envVars = import.meta.env as Record<string, unknown>;
const TEAMS_API_PREFIX = (envVars['VITE_TEAMS_API_PREFIX'] as string) || 'https://api.ntdoc.site/api/v1/teams';
const TEAM_MEMBERS_API_PREFIX = (envVars['VITE_TEAMS_MEMBER_API_PREFIX'] as string) || 'http://localhost:8070/api/v1/teamMembers';

function mapMemberRole(raw: unknown): TeamRole {
  const s = typeof raw === 'string' ? raw.toLowerCase() : '';
  if (s.includes('owner')) return 'owner';
  if (s.includes('admin')) return 'admin';
  return 'member';
}

// Safe helpers for unknown
const asObj = (v: unknown): Record<string, unknown> => (v && typeof v === 'object' ? (v as Record<string, unknown>) : {});
const getNum = (o: Record<string, unknown>, k: string): number | undefined => (typeof o[k] === 'number' ? (o[k] as number) : undefined);
const getStr = (o: Record<string, unknown>, k: string): string | undefined => (typeof o[k] === 'string' ? (o[k] as string) : undefined);

function normalizeTeam(raw: unknown): Team {
  const r = asObj(raw);
  return {
    id: String(r['teamId'] ?? r['id'] ?? Math.random().toString(36).slice(2)),
    name: (getStr(r, 'name') ?? 'Unnamed Team'),
    description: (getStr(r, 'description') ?? ''),
    memberCount: Number(r['memberCount'] ?? 0),
    avatar: getStr(r, 'avatarUrl') ?? undefined,
    documentCount: Number((r['documentCount'] ?? r['docsCount'] ?? 0) as unknown),
  };
}

function normalizeMember(raw: unknown): TeamMember {
  const r = asObj(raw);
  return {
    id: String(r['memberId'] ?? r['id'] ?? r['userId'] ?? Math.random().toString(36).slice(2)),
    name: (getStr(r, 'displayName') ?? getStr(r, 'name') ?? getStr(r, 'username') ?? 'Unknown'),
    email: (getStr(r, 'email') ?? getStr(r, 'mail') ?? ''),
    role: mapMemberRole(r['role'] ?? r['memberRole']),
    avatar: (getStr(r, 'avatarUrl') ?? getStr(r, 'avatar') ?? undefined),
    status: 'offline',
  };
}

export class HttpTeamRepository implements ITeamRepository {
  private readonly teamsEndpoint: string;
  private readonly membersEndpoint: string;
  constructor(teamsEndpoint: string = TEAMS_API_PREFIX, membersEndpoint: string = TEAM_MEMBERS_API_PREFIX) {
    this.teamsEndpoint = teamsEndpoint;
    this.membersEndpoint = membersEndpoint;
  }

  async getTeams(options?: { activeOnly?: boolean }): Promise<Team[]> {
    const query = options?.activeOnly === false ? '' : '?activeOnly=true';
    const resp = await http.get<unknown>(`${this.teamsEndpoint}${query}`);
    const respObj = asObj(resp);
    const body = asObj(respObj['data'] ?? respObj);
    const dataField = body['data'];
    const membersFromData = asObj(dataField)['teams'];
    const list: unknown = Array.isArray(membersFromData)
      ? membersFromData
      : Array.isArray(body['teams'])
        ? body['teams']
        : Array.isArray(dataField)
          ? dataField
          : [];
    const arr: unknown[] = Array.isArray(list) ? (list as unknown[]) : [];
    return arr.map(normalizeTeam);
  }

  async createTeam(input: { name: string; description?: string }): Promise<Team> {
    const resp = await http.post<unknown, { name: string; description?: string }>(`${this.teamsEndpoint}`, input);
    const respObj = asObj(resp);
    const body = asObj(respObj['data'] ?? respObj);
    const status = getNum(respObj, 'status');
    const ok = (status !== undefined && status >= 200 && status < 300) || respObj['success'] === true || body['success'] === true || getNum(body, 'code') === 0;
    if (!ok) throw new Error(getStr(body, 'message') || 'Create team failed');
    const data = asObj(body['data']);
    return normalizeTeam({ ...data, name: data['name'] ?? input.name, description: data['description'] ?? input.description });
  }

  async updateTeam(teamId: string, input: { name: string; description?: string }): Promise<Team> {
    const resp = await http.put<unknown, { name: string; description?: string }>(`${this.teamsEndpoint}/${encodeURIComponent(teamId)}`, input);
    const respObj = asObj(resp);
    const body = asObj(respObj['data'] ?? respObj);
    const status = getNum(respObj, 'status');
    const ok = (status !== undefined && status >= 200 && status < 300) || respObj['success'] === true || body['success'] === true || getNum(body, 'code') === 0;
    if (!ok) throw new Error(getStr(body, 'message') || 'Update team failed');
    const data = asObj(body['data']);
    return normalizeTeam({ teamId, ...data, name: data['name'] ?? input.name, description: data['description'] ?? input.description });
  }

  async deleteTeam(teamId: string): Promise<{ success: boolean; message?: string }> {
    const resp = await http.delete<unknown>(`${this.teamsEndpoint}/${encodeURIComponent(teamId)}`);
    const respObj = asObj(resp);
    const body = asObj(respObj['data'] ?? respObj);
    const status = getNum(respObj, 'status');
    const ok = (status !== undefined && status >= 200 && status < 300) || respObj['success'] === true || body['success'] === true || getNum(body, 'code') === 0;
    return { success: !!ok, message: getStr(body, 'message') };
  }

  async getMembers(teamId: string): Promise<TeamMember[]> {
    const url = `${this.membersEndpoint}/${encodeURIComponent(teamId)}/memberList`;
    const resp = await http.get<unknown>(url);
    const respObj = asObj(resp);
    const body = asObj(respObj['data'] ?? respObj);
    const dataUnknown = body['data'];
    const membersFromData = asObj(dataUnknown)['members'];
    const list: unknown = Array.isArray(membersFromData)
      ? membersFromData
      : Array.isArray(body['members'])
        ? body['members']
        : Array.isArray(dataUnknown)
          ? dataUnknown
          : [];
    const arr: unknown[] = Array.isArray(list) ? (list as unknown[]) : [];
    return arr.map(normalizeMember);
  }

  async inviteMember(teamId: string, userEmail: string, role: TeamRole): Promise<TeamMember | null> {
    const payload: { userEmail: string; role: string } = { userEmail, role: role.toUpperCase() };
    const url = `${this.membersEndpoint}/${encodeURIComponent(teamId)}/addMembers`;
    const resp = await http.post<unknown, typeof payload>(url, payload);
    const respObj = asObj(resp);
    const body = asObj(respObj['data'] ?? respObj);
    const status = getNum(respObj, 'status');
    const ok = (status !== undefined && status >= 200 && status < 300) || respObj['success'] === true || body['success'] === true || getNum(body, 'code') === 0;
    if (!ok) throw new Error(getStr(body, 'message') || 'Invite member failed');
    const data = body['data'];
    return data ? normalizeMember(data) : null;
  }

  async removeMember(teamId: string, memberId: string): Promise<{ success: boolean; message?: string }> {
    const url = `${this.membersEndpoint}/${encodeURIComponent(teamId)}/removeMember/${encodeURIComponent(memberId)}`;
    const resp = await http.delete<unknown>(url);
    const respObj = asObj(resp);
    const body = asObj(respObj['data'] ?? respObj);
    const status = getNum(respObj, 'status');
    const ok = (status !== undefined && status >= 200 && status < 300) || respObj['success'] === true || body['success'] === true || getNum(body, 'code') === 0;
    return { success: !!ok, message: getStr(body, 'message') };
  }
}

let currentRepo: ITeamRepository = new HttpTeamRepository();
export function setTeamRepository(repo: ITeamRepository) { currentRepo = repo; }
export function getTeamRepository(): ITeamRepository { return currentRepo; }
