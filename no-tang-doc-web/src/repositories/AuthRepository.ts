// filepath: d:\Code\NoTangDoc\no-tang-doc-web\src\repositories\AuthRepository.ts
/**
 * Auth Repository (Repository Pattern)
 * Encapsulates auth-related HTTP calls (exchange code, refresh, revoke).
 * UI / context code should depend on this abstraction rather than raw fetch.
 */

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

export interface IAuthRepository {
  exchangeAuthorizationCode(params: {
    code: string;
    codeVerifier: string;
    redirectUri: string;
    nonce: string;
  }): Promise<ExchangeResponse>;
  refreshTokens(refreshToken: string): Promise<ExchangeResponse>;
  revokeSession(accessToken?: string, refreshToken?: string | null, idToken?: string | null): Promise<boolean>;
}

const API_BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
function buildUrl(path: string) { return API_BASE ? API_BASE + path : path; }

export class HttpAuthRepository implements IAuthRepository {
  async exchangeAuthorizationCode(params: {
    code: string;
    codeVerifier: string;
    redirectUri: string;
    nonce: string;
  }): Promise<ExchangeResponse> {
    try {
      const res = await fetch(buildUrl('/api/auth/exchange'), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(params),
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
          id_token: data.id_token,
        },
      };
    } catch (e: unknown) {
      return { success: false, error: e?.message || 'network_error' };
    }
  }

  async refreshTokens(refreshToken: string): Promise<ExchangeResponse> {
    try {
      const res = await fetch(buildUrl('/api/auth/refresh'), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });
      if (!res.ok) return { success: false, error: `HTTP ${res.status}` };
      const data = await res.json().catch(() => ({}));
      if (data.error) return { success: false, error: data.error };
      if (!data.access_token) return { success: false, error: 'missing_access_token' };
      return {
        success: true,
        tokens: {
          access_token: data.access_token,
          refresh_token: data.refresh_token || refreshToken,
          expires_in: data.expires_in ?? 300,
          refresh_expires_in: data.refresh_expires_in,
          token_type: data.token_type,
          id_token: data.id_token,
        },
      };
    } catch (e: unknown) {
      return { success: false, error: e?.message || 'network_error' };
    }
  }

  async revokeSession(accessToken?: string, refreshToken?: string | null, idToken?: string | null): Promise<boolean> {
    if (!refreshToken && !idToken) return true;
    try {
      const res = await fetch(buildUrl('/api/auth/logout'), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
        },
        body: JSON.stringify({ refreshToken: refreshToken || undefined, idToken: idToken || undefined }),
      });
      if (!res.ok) return false;
      const data = await res.json().catch(() => ({}));
      return !!data.success;
    } catch {
      return false;
    }
  }
}

let currentRepo: IAuthRepository = new HttpAuthRepository();
export function setAuthRepository(repo: IAuthRepository) { currentRepo = repo; }
export function getAuthRepository(): IAuthRepository { return currentRepo; }

