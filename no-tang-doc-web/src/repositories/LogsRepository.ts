// filepath: d:\Code\NoTangDoc\no-tang-doc-web\src\repositories\LogsRepository.ts
/**
 * Logs Repository - abstracts activity log data access.
 */
import { http } from '../utils/request';

export interface LogEntry {
  id: number;
  actorType: string;
  actorName: string;
  userId: number;
  operationType: string;
  targetId: number;
  targetName: string;
  operationStatus: 'SUCCESS' | 'FAILURE';
  message: string | null;
  time: string;
}

export interface ILogsRepository {
  getAllLogs(): Promise<LogEntry[]>;
}

const LOGS_LIST_ALL = (import.meta.env as unknown)?.VITE_LOGS_LIST_ALL || '/api/v1/logs';

export class HttpLogsRepository implements ILogsRepository {
  private readonly listEndpoint: string;
  constructor(listEndpoint: string = LOGS_LIST_ALL) {
    this.listEndpoint = listEndpoint;
  }
  async getAllLogs(): Promise<LogEntry[]> {
    const resp: unknown = await http.get(this.listEndpoint);
    const data = resp?.data ?? resp;
    if (!Array.isArray(data)) return [];
    // Basic normalization (ensure required fields)
    return data.map((l: unknown, i: number) => ({
      id: Number(l?.id ?? i + 1),
      actorType: String(l?.actorType ?? l?.userType ?? 'UNKNOWN'),
      actorName: String(l?.actorName ?? l?.username ?? 'Unknown'),
      userId: Number(l?.userId ?? 0),
      operationType: String(l?.operationType ?? l?.opType ?? 'UNKNOWN'),
      targetId: Number(l?.targetId ?? 0),
      targetName: String(l?.targetName ?? l?.objectName ?? ''),
      operationStatus: (l?.operationStatus === 'FAILURE' ? 'FAILURE' : 'SUCCESS'),
      message: l?.message ?? null,
      time: String(l?.time ?? l?.timestamp ?? new Date().toISOString()),
    })) as LogEntry[];
  }
}

let currentRepo: ILogsRepository = new HttpLogsRepository();
export function setLogsRepository(repo: ILogsRepository) { currentRepo = repo; }
export function getLogsRepository(): ILogsRepository { return currentRepo; }

