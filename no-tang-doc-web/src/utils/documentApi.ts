/**
 * Document API utilities
 *
 * This file contains functions for interacting with your backend API.
 * Replace the mock implementations with actual API calls to your backend.
 */

import { http } from './request';

interface Document {
  id: string;
  name: string;
  type: string;
  size: string;
  uploadDate: string;
  category: string;
  tags: string[];
  sizeBytes?: number; // raw size in bytes for accurate aggregates
}

// Backend payload contracts
interface BackendDocument {
  documentId: string;
  fileName: string;
  fileSize: number; // bytes
  mimeType: string;
  description?: string;
  uploadTime: string; // ISO string
  lastModified?: string;
}

interface BackendListResp {
  code: number;
  message?: string;
  data?: { documents?: BackendDocument[] } | null;
  documents?: BackendDocument[]; // fallback if backend returns at top-level
}

const DOCS_ENDPOINT = import.meta.env.VITE_DOCS_API_PREFIX;

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
  // Fallback: take subtype tail
  const slash = mime.indexOf('/');
  if (slash > 0 && slash < mime.length - 1) {
    const subtype = mime.slice(slash + 1).toLowerCase();
    // Prefer the last token after '.' for vnd.* types
    const parts = subtype.split('.');
    return (parts[parts.length - 1] || '').replace(/[^a-z0-9]/g, '') || '';
  }
  return '';
}

function mapBackendToDocument(d: BackendDocument): Document {
  const ext = getFileExtension(d.fileName) || extensionFromMimeType(d.mimeType) || 'unknown';
  return {
    id: d.documentId,
    name: d.fileName,
    type: ext, // use file extension as Type field (e.g., pdf, md)
    size: formatBytes(d.fileSize),
    uploadDate: d.uploadTime,
    // Fields not provided by backend yet; keep placeholders to preserve UI data shape
    category: 'General',
    tags: [],
    sizeBytes: typeof d.fileSize === 'number' ? d.fileSize : undefined,
  };
}

/**
 * Advanced search function that calls your backend API
 *
 * To integrate with your real backend:
 * 1. Replace the mock data with an actual fetch/axios call to your API endpoint
 * 2. Update the endpoint URL to match your backend
 * 3. Add authentication headers if needed
 */
export const advancedSearch = async (query: string): Promise<Document[]> => {
  // For now, perform client-side filtering after fetching all documents.
  const all = await getAllDocuments();
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
};

/**
 * Get all documents from backend and map to UI shape
 */
export const getAllDocuments = async (): Promise<Document[]> => {
  // Call the real backend. http helper attaches JWT automatically from localStorage.
  const resp = await http.get<BackendListResp>(DOCS_ENDPOINT);
  const data = (resp as any)?.data ?? resp; // unwrap ApiResponse.data if wrapped
  const docs = (data?.documents || (resp as any)?.documents || []) as BackendDocument[];

  // Treat code 0 or 200 (or undefined) as success; throw on explicit non-success codes
  const rawCode = (resp as any)?.code;
  const numericCode = typeof rawCode === 'string' ? parseInt(rawCode, 10) : rawCode;
  if (numericCode !== undefined && numericCode !== 0 && numericCode !== 200) {
    throw new Error((resp as any)?.message || 'Failed to fetch documents');
  }

  return (docs || []).map(mapBackendToDocument);
};

/**
 * Example function for searching documents by tags
 */
export const searchByTags = async (tags: string[]): Promise<Document[]> => {
  // Placeholder: backend integration can be added later. For now, filter client-side.
  const all = await getAllDocuments();
  if (!tags?.length) return all;
  const tagSet = new Set(tags.map((t) => t.toLowerCase()));
  return all.filter((d) => d.tags.some((t) => tagSet.has(t.toLowerCase())));
};