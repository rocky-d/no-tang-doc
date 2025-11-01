// Generic HTTP client that automatically attaches JWT from localStorage
// Usage: import { http } from './request'; then http.get('/api/xxx')

const API_BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
const PERSIST_KEY = 'auth_tokens_v1';

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface RequestOptions<TBody = unknown> {
  method?: HttpMethod;
  body?: TBody;
  headers?: Record<string, string>;
  withAuth?: boolean; // attach Authorization: Bearer <token>
  timeoutMs?: number; // abort after timeout
}

interface StoredTokens {
  access_token: string;
  access_expires_at: number; // epoch ms
}

function buildUrl(path: string) {
  if (!path) return path;
  if (path.startsWith('http://') || path.startsWith('https://')) return path;
  return API_BASE ? API_BASE + path : path;
}

function getValidAccessToken(): string | null {
  try {
    const raw = localStorage.getItem(PERSIST_KEY);
    if (!raw) return null;
    const parsed: StoredTokens = JSON.parse(raw);
    if (!parsed?.access_token || !parsed?.access_expires_at) return null;
    if (parsed.access_expires_at <= Date.now()) return null; // expired
    return parsed.access_token;
  } catch {
    return null;
  }
}

export async function request<TResponse = unknown, TBody = unknown>(
  path: string,
  opts: RequestOptions<TBody> = {}
): Promise<TResponse> {
  const { method = 'GET', body, headers = {}, withAuth = true, timeoutMs = 20000 } = opts;

  const controller = new AbortController();
  const timer = timeoutMs > 0 ? window.setTimeout(() => controller.abort(), timeoutMs) : undefined;

  const finalHeaders: Record<string, string> = { Accept: 'application/json', ...headers };

  const hasBody = body !== undefined && body !== null && method !== 'GET';
  const isFormData = typeof FormData !== 'undefined' && body instanceof FormData;
  if (hasBody && !isFormData) {
    finalHeaders['Content-Type'] = finalHeaders['Content-Type'] || 'application/json';
  }

  if (withAuth) {
    const token = getValidAccessToken();
    if (token) finalHeaders['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(buildUrl(path), {
    method,
    headers: finalHeaders,
    body: hasBody ? (isFormData ? (body as unknown) : JSON.stringify(body)) : undefined,
    signal: controller.signal,
  }).finally(() => {
    if (timer) window.clearTimeout(timer);
  });

  if (!res.ok) {
    // Best-effort error payload
    let detail = '';
    try { detail = await res.text(); } catch { /* ignore */ }
    throw new Error(`HTTP ${res.status}: ${detail || res.statusText}`);
  }

  const contentType = res.headers.get('content-type') || '';
  if (contentType.includes('application/json')) return (await res.json()) as TResponse;
  if (contentType.startsWith('text/')) return (await res.text()) as unknown as TResponse;
  return (await res.blob()) as unknown as TResponse;
}

export const http = {
  get: <T = unknown>(path: string, opts?: Omit<RequestOptions, 'method' | 'body'>) =>
    request<T>(path, { ...opts, method: 'GET' }),
  post: <T = unknown, B = unknown>(path: string, body?: B, opts?: Omit<RequestOptions<B>, 'method'>) =>
    request<T, B>(path, { ...opts, method: 'POST', body }),
  put: <T = unknown, B = unknown>(path: string, body?: B, opts?: Omit<RequestOptions<B>, 'method'>) =>
    request<T, B>(path, { ...opts, method: 'PUT', body }),
  patch: <T = unknown, B = unknown>(path: string, body?: B, opts?: Omit<RequestOptions<B>, 'method'>) =>
    request<T, B>(path, { ...opts, method: 'PATCH', body }),
  delete: <T = unknown>(path: string, opts?: Omit<RequestOptions, 'method' | 'body'>) =>
    request<T>(path, { ...opts, method: 'DELETE' }),
};

// Optional helper: Call your backend's /api/auth/me
export const getMyProfile = () => http.get('/api/auth/me');

