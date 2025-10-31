// Stateless JWT auth API helpers
// Only responsibilities: exchange authorization code, refresh tokens, decode JWT.
// Backend must return access_token (+ optional refresh_token) in JSON (no cookies).

const API_BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
function buildUrl(path: string) { return API_BASE ? API_BASE + path : path; }

export interface TokenSet {
  access_token: string;
  refresh_token?: string;
  expires_in: number;
  refresh_expires_in?: number;
  token_type?: string;
  id_token?: string;
}

export interface ExchangeResponse {
  success: boolean;
  error?: string;
  tokens?: TokenSet;
}

export async function exchangeAuthorizationCode(params: {
  code: string;
  codeVerifier: string;
  redirectUri: string;
  nonce: string;
}): Promise<ExchangeResponse> {
  try {
    const res = await fetch(buildUrl('/api/auth/exchange'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(params)
    });
    if (!res.ok) return { success: false, error: `HTTP ${res.status}` };
    const data = await res.json().catch(() => ({}));
    if (data.error) return { success: false, error: data.error };
    if (!data.access_token) return { success: false, error: 'missing_access_token' };
    return {
      success: true,
      tokens: {
        access_token: data.access_token,
        refresh_token: data.refresh_token,
        expires_in: data.expires_in ?? 300,
        refresh_expires_in: data.refresh_expires_in,
        token_type: data.token_type,
        id_token: data.id_token
      }
    };
  } catch (e: any) {
    return { success: false, error: e.message || 'network_error' };
  }
}

export async function refreshTokens(refreshToken: string): Promise<ExchangeResponse> {
  try {
    console.log('[auth][refresh] start with refresh_token len=', refreshToken?.length);
    // 后端接受字段名 refreshToken (camelCase)，之前发送 refresh_token 导致反序列化失败
    const res = await fetch(buildUrl('/api/auth/refresh'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }) // 修正字段名
    });
    console.log('[auth][refresh] HTTP status =', res.status);
    if (!res.ok) return { success: false, error: `HTTP ${res.status}` };
    const data = await res.json().catch(() => ({}));
    console.log('[auth][refresh] response json =', data);
    if (data.error) return { success: false, error: data.error };
    if (!data.access_token) return { success: false, error: 'missing_access_token' };
    return {
      success: true,
      tokens: {
        access_token: data.access_token,
        // 后端透传 Keycloak 响应（Keycloak 使用 refresh_token 下划线命名），因此仍从 data.refresh_token 读取
        refresh_token: data.refresh_token || refreshToken,
        expires_in: data.expires_in ?? 300,
        refresh_expires_in: data.refresh_expires_in,
        token_type: data.token_type,
        id_token: data.id_token
      }
    };
  } catch (e: any) {
    console.error('[auth][refresh] network / code error', e);
    return { success: false, error: e.message || 'network_error' };
  }
}

export function decodeJwt<T = any>(token?: string): T | null {
  if (!token) return null;
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  try {
    const payload = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decodeURIComponent(escape(payload)));
  } catch {
    return null;
  }
}

// Helper for authorized fetch (optional future use)
export async function authFetch(input: RequestInfo | URL, init: RequestInit = {}, accessToken?: string) {
  const headers = new Headers(init.headers || {});
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`);
  return fetch(input, { ...init, headers });
}

export async function revokeSession(accessToken?: string, refreshToken?: string | null, idToken?: string | null): Promise<boolean> {
  if (!refreshToken && !idToken) return true; // nothing to revoke
  try {
    const res = await fetch(buildUrl('/api/auth/logout'), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {})
      },
      body: JSON.stringify({ refreshToken: refreshToken || undefined, idToken: idToken || undefined })
    });
    if (!res.ok) return false;
    const data = await res.json().catch(() => ({}));
    return !!data.success;
  } catch {
    return false;
  }
}
