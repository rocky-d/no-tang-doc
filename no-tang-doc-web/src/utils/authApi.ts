// Stateless JWT auth API helpers
// Only responsibilities: exchange authorization code, refresh tokens, decode JWT.
// Backend must return access_token (+ optional refresh_token) in JSON (no cookies).

import { getAuthRepository } from '../repositories/AuthRepository';
export type { TokenSet, ExchangeResponse } from '../repositories/AuthRepository';

export async function exchangeAuthorizationCode(params: {
  code: string;
  codeVerifier: string;
  redirectUri: string;
  nonce: string;
}) {
  const repo = getAuthRepository();
  return repo.exchangeAuthorizationCode(params);
}

export async function refreshTokens(refreshToken: string) {
  const repo = getAuthRepository();
  return repo.refreshTokens(refreshToken);
}

export function decodeJwt<T = unknown>(token?: string): T | null {
  if (!token) return null;
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  try {
    const payload = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
    // decodeURIComponent(escape()) is deprecated; but keeping for compatibility
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
  const repo = getAuthRepository();
  return repo.revokeSession(accessToken, refreshToken, idToken);
}
