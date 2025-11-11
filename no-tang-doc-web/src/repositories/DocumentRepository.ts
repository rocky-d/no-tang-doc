// filepath: d:\Code\NoTangDoc\no-tang-doc-web\src\repositories\DocumentRepository.ts
/**
 * Document Repository (Repository Pattern)
 *
 * This layer abstracts data access for documents. UI imports this instead of calling HTTP directly.
 * Provides an interface for easy swapping (e.g., mocks in tests, different backends).
 */

import { http } from '../utils/request';

// Domain model exposed to UI
export interface Document {
  id: string;
  name: string;
  type: string; // file extension (pdf, docx, ...)
  size: string; // human-readable
  uploadDate: string; // ISO or display string
  category: string; // placeholder until backend supports
  tags: string[];
  sizeBytes?: number; // raw bytes for aggregates
}

// Backend payload contracts (kept internal to repo)
interface BackendDocument {
  documentId: string;
  fileName: string;
  fileSize: number; // bytes
  mimeType: string;
  description?: string;
  uploadTime: string; // ISO string
  lastModified?: string;
  tags?: string[]; // backend-provided tags
}

interface BackendListResp {
  code: number | string;
  message?: string;
  data?: { documents?: BackendDocument[] } | null;
  documents?: BackendDocument[]; // fallback if backend returns at top-level
}

const DOCS_ENDPOINT = (import.meta.env.VITE_DOCS_API_PREFIX as string) || '/api/v1/documents';

// Utilities (kept close to the mapping logic)
function formatBytes(bytes: number | null | undefined): string {
  const b = typeof bytes === 'number' && isFinite(bytes) && bytes >= 0 ? bytes : 0;
  if (b === 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(b) / Math.log(1024));
  const value = b / Math.pow(1024, i);
  return `${value.toFixed(value < 10 ? 1 : 0)} ${units[i]}`;
}

function getFileExtension(name?: string | null): string {
  if (!name) return '';
  const clean = name.split('?')[0].split('#')[0];
  const idx = clean.lastIndexOf('.');
  if (idx <= 0 || idx === clean.length - 1) return '';
  return clean.slice(idx + 1).toLowerCase();
}

function extensionFromMimeType(mime?: string | null): string {
  if (!mime) return '';
  const map: Record<string, string> = {
    'application/pdf': 'pdf',
    'application/msword': 'doc',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
    'application/vnd.ms-excel': 'xls',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
    'application/vnd.ms-powerpoint': 'ppt',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'pptx',
    'text/plain': 'txt',
    'text/markdown': 'md',
    'text/csv': 'csv',
    'image/png': 'png',
    'image/jpeg': 'jpg',
    'image/gif': 'gif',
    'image/webp': 'webp',
    'image/svg+xml': 'svg',
    'application/zip': 'zip',
    'application/x-7z-compressed': '7z',
    'application/x-rar-compressed': 'rar',
    'application/octet-stream': 'bin',
  };
  if (map[mime]) return map[mime];
  const slash = mime.indexOf('/');
  if (slash > 0 && slash < mime.length - 1) {
    const subtype = mime.slice(slash + 1).toLowerCase();
    const parts = subtype.split('.');
    return (parts[parts.length - 1] || '').replace(/[^a-z0-9]/g, '') || '';
  }
  return '';
}

function mapBackendToDocument(d: BackendDocument): Document {
  const ext = getFileExtension(d.fileName) || extensionFromMimeType(d.mimeType) || 'unknown';
  const rawTags = Array.isArray(d.tags) ? d.tags : [];
  const tags = rawTags.map((t) => (t ?? '').trim()).filter(Boolean);
  return {
    id: String(d.documentId),
    name: d.fileName || 'Untitled',
    type: ext || 'unknown',
    size: formatBytes(d.fileSize),
    uploadDate: d.uploadTime,
    category: 'General',
    tags,
    sizeBytes: d.fileSize,
  };
}

// Repository contract
export interface IDocumentRepository {
  getAll(): Promise<Document[]>;
  advancedSearch(query: string): Promise<Document[]>;
  searchByTags(tags: string[]): Promise<Document[]>;
  // New actions used across UI
  getDownloadInfo(documentId: string): Promise<{ url: string; fileName?: string | null }>;
  delete(documentId: string): Promise<{ success: boolean; message?: string }>;
  getShareUrl(documentId: string): Promise<string>;
  getComments(documentId: string): Promise<Array<{ id: string; user: string; avatar?: string; content: string; timestamp: string }>>;
  addComment(documentId: string, content: string): Promise<{ id: string; user: string; avatar?: string; content: string; timestamp: string }>;
  updateTags(documentId: string, tags: string[]): Promise<string[]>;
}

// Default HTTP-backed implementation
export class HttpDocumentRepository implements IDocumentRepository {
  private readonly endpoint: string;

  constructor(endpoint: string = DOCS_ENDPOINT) {
    this.endpoint = endpoint;
  }

  async getAll(): Promise<Document[]> {
    const resp = await http.get<BackendListResp>(this.endpoint);

    // Accept multiple nesting styles: either resp.data.documents or resp.documents
    const docs: BackendDocument[] = (
      (resp.data && Array.isArray(resp.data.documents) ? resp.data.documents : undefined) ||
      (Array.isArray(resp.documents) ? resp.documents : [])
    );

    const numericCode = typeof resp.code === 'string' ? parseInt(resp.code as unknown as string, 10) : resp.code;
    if (numericCode !== undefined && numericCode !== 0 && numericCode !== 200) {
      throw new Error(resp.message || 'Failed to fetch documents');
    }

    return docs.map(mapBackendToDocument);
  }

  async advancedSearch(query: string): Promise<Document[]> {
    // For now, perform client-side filtering after fetching all documents.
    const all = await this.getAll();
    const q = query.trim().toLowerCase();
    if (!q) return all;
    return all.filter((doc) => {
      const nameMatch = doc.name.toLowerCase().includes(q);
      const typeMatch = doc.type.toLowerCase().includes(q);
      const categoryMatch = doc.category.toLowerCase().includes(q);
      const tagMatch = doc.tags.some((t) => t.toLowerCase().includes(q));
      const yearMatch = /\b(20\d{2})\b/.test(q);
      const hasYear = /\b(20\d{2})\b/.test(doc.uploadDate);
      return nameMatch || typeMatch || categoryMatch || tagMatch || (yearMatch && hasYear);
    });
  }

  async searchByTags(tags: string[]): Promise<Document[]> {
    const all = await this.getAll();
    if (!tags?.length) return all;
    const tagSet = new Set(tags.map((t) => t.toLowerCase()));
    return all.filter((d) => d.tags.some((t) => tagSet.has(t.toLowerCase())));
  }

  async getDownloadInfo(documentId: string): Promise<{ url: string; fileName?: string | null }> {
    const resp: unknown = await http.get(`${this.endpoint}/download/${encodeURIComponent(documentId)}`);
    const data: unknown = resp?.data ?? resp;
    const d: unknown = data?.data ?? data;
    const url: string | undefined = d?.url || d?.downloadUrl || data?.url || data?.downloadUrl;
    const fileName: string | undefined = d?.fileName;
    if (!url) throw new Error('download url not found');
    return { url, fileName: fileName ?? null };
  }

  async delete(documentId: string): Promise<{ success: boolean; message?: string }> {
    const resp: unknown = await http.delete(`${this.endpoint}/${encodeURIComponent(documentId)}`);
    const ok = (resp?.status && resp.status >= 200 && resp.status < 300) || resp?.data?.success !== false;
    const msg = resp?.data?.message || resp?.message;
    return { success: !!ok, message: msg };
  }

  async getShareUrl(documentId: string): Promise<string> {
    const resp: unknown = await http.get(`${this.endpoint}/share?documentId=${encodeURIComponent(documentId)}`);
    const url: string | undefined = resp?.data?.url ?? resp?.url;
    if (!url) throw new Error('share url not found');
    return url;
  }

  async getComments(documentId: string): Promise<Array<{ id: string; user: string; avatar?: string; content: string; timestamp: string }>> {
    const resp: unknown = await http.get(`${this.endpoint}/${encodeURIComponent(documentId)}/comments`);
    const body: unknown = resp?.data ?? resp ?? {};
    const list = body?.data?.comments ?? body?.comments ?? body?.data ?? [];
    const arr: unknown[] = Array.isArray(list) ? list : [];
    return arr.map((c: unknown) => ({
      id: String(c?.id ?? Math.random().toString(36).slice(2)),
      user: c?.username ?? c?.userEmail ?? 'Unknown',
      avatar: undefined,
      content: c?.content ?? '',
      timestamp: c?.createdAt ? new Date(c.createdAt).toLocaleString() : new Date().toLocaleString(),
    }));
  }

  async addComment(documentId: string, content: string): Promise<{ id: string; user: string; avatar?: string; content: string; timestamp: string }> {
    const resp: unknown = await http.post(`${this.endpoint}/${encodeURIComponent(documentId)}/comments`, { documentId, content });
    const body: unknown = resp?.data ?? resp ?? {};
    const ok = (typeof resp?.code === 'number' ? (resp.code >= 200 && resp.code < 300) : false) || body?.success === true || body?.code === 0;
    if (!ok) throw new Error(body?.message || 'Failed to add comment');
    const c: unknown = body?.data ?? {};
    return {
      id: String(c?.id ?? Date.now()),
      user: c?.username ?? c?.userEmail ?? 'Me',
      avatar: undefined,
      content: c?.content ?? content,
      timestamp: c?.createdAt ? new Date(c.createdAt).toLocaleString() : new Date().toLocaleString(),
    };
  }

  async updateTags(documentId: string, tags: string[]): Promise<string[]> {
    const resp: unknown = await http.post(`${this.endpoint}/${encodeURIComponent(documentId)}/tags`, tags);
    const body: unknown = resp?.data ?? resp ?? {};
    const ok = (typeof resp?.code === 'number' ? (resp.code >= 200 && resp.code < 300) : false) || body?.success === true || body?.code === 0;
    if (!ok) throw new Error(body?.message || 'Failed to update tags');
    const data: unknown = body?.data ?? {};
    const newTags: string[] = Array.isArray(data?.tags) ? data.tags : tags;
    return newTags;
  }
}

// Simple registry for DI / swap in tests
let currentRepository: IDocumentRepository = new HttpDocumentRepository();

export function setDocumentRepository(repo: IDocumentRepository) {
  // Allows swapping implementation (e.g., MockDocumentRepository) in tests or at runtime
  currentRepository = repo;
}

export function getDocumentRepository(): IDocumentRepository {
  return currentRepository;
}
