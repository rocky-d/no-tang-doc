import React, { createContext, useContext, useEffect, useState, useCallback, useRef } from 'react';
import { generateCodeVerifier, generateCodeChallenge, generateState, generateNonce, storeTemp, readTemp, clearTemp } from '../utils/pkce';
import { exchangeAuthorizationCode, refreshTokens, decodeJwt, revokeSession } from '../utils/authApi';

interface OIDCUser {
  id: string;
  username?: string;
  name?: string;
  email?: string;
  roles?: string[];
}

interface AuthContextType {
  isLoading: boolean;
  user: OIDCUser | null;
  isAuthenticated: boolean;
  accessToken: string | null;
  refreshToken: string | null;
  login: (redirectPath?: string) => Promise<void>;
  register: (redirectPath?: string) => Promise<void>;
  logout: () => Promise<void>;
  completeLogin: (tokens: { access_token: string; refresh_token?: string; expires_in: number; id_token?: string; refresh_expires_in?: number }) => void;
  hasRole: (role: string) => boolean;
  error: string | null;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const KC_BASE = (import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8080/').replace(/\/?$/, '/');
const KC_REALM = import.meta.env.VITE_KEYCLOAK_REALM || 'your-realm';
const KC_CLIENT_ID = import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'your-client-id';
const AUTH_ENDPOINT = `${KC_BASE}realms/${KC_REALM}/protocol/openid-connect/auth`;
const OIDC_SCOPE = (import.meta.env.VITE_OIDC_SCOPES || 'openid profile email').trim();
const PERSIST_KEY = 'auth_tokens_v1';

interface PersistedTokens {
  access_token: string;
  refresh_token?: string;
  access_expires_at: number; // epoch ms
  refresh_expires_at?: number;
  id_token?: string;
}

function buildRedirectUri() { return window.location.origin + '/auth/callback'; }

function parseUserFromTokens(accessToken?: string, idToken?: string): OIDCUser | null {
  const tokenToParse = idToken || accessToken;
  if (!tokenToParse) return null;
  const payload: any = decodeJwt(tokenToParse);
  if (!payload) return null;
  const roles: string[] = payload.realm_access?.roles || payload.resource_access?.[KC_CLIENT_ID]?.roles || [];
  return {
    id: payload.sub,
    username: payload.preferred_username,
    name: payload.name || payload.preferred_username,
    email: payload.email,
    roles
  };
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<OIDCUser | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [refreshToken, setRefreshToken] = useState<string | null>(null);
  const [accessExpiresAt, setAccessExpiresAt] = useState<number | null>(null);
  const [refreshExpiresAt, setRefreshExpiresAt] = useState<number | null>(null);
  const [idToken, setIdToken] = useState<string | undefined>(undefined);
  const [error, setError] = useState<string | null>(null);
  const refreshTimer = useRef<number | null>(null);
  const loggedOut = useRef(false);

  const persistTokens = useCallback((tokens: PersistedTokens | null) => {
    if (!tokens) {
      localStorage.removeItem(PERSIST_KEY);
      return;
    }
    localStorage.setItem(PERSIST_KEY, JSON.stringify(tokens));
  }, []);

  const applyTokens = useCallback((t: { access_token: string; refresh_token?: string; expires_in: number; id_token?: string; refresh_expires_in?: number }) => {
    if (loggedOut.current) return;
    const now = Date.now();
    const accessExp = now + t.expires_in * 1000;
    const refreshExp = t.refresh_expires_in ? now + t.refresh_expires_in * 1000 : undefined;
    setAccessToken(t.access_token);
    setRefreshToken(t.refresh_token || null);
    setAccessExpiresAt(accessExp);
    setRefreshExpiresAt(refreshExp || null);
    setIdToken(t.id_token);
    const u = parseUserFromTokens(t.access_token, t.id_token);
    setUser(u);
    persistTokens({
      access_token: t.access_token,
      refresh_token: t.refresh_token,
      access_expires_at: accessExp,
      refresh_expires_at: refreshExp,
      id_token: t.id_token
    });
  }, [persistTokens]);

  // 提前定义 logout，避免下方 useEffect 依赖数组在声明前访问它
  const logout = useCallback(async () => {
    if (loggedOut.current) return;
    loggedOut.current = true;
    setError(null);
    if (refreshTimer.current) window.clearTimeout(refreshTimer.current);
    try {
      await revokeSession(accessToken || undefined, refreshToken || undefined, idToken || undefined);
    } catch { /* ignore */ }
    setUser(null);
    setAccessToken(null);
    setRefreshToken(null);
    setAccessExpiresAt(null);
    setRefreshExpiresAt(null);
    setIdToken(undefined);
    persistTokens(null);
    clearTemp(['pkce_verifier','oidc_state','oidc_nonce','post_login_redirect']);
    window.location.replace('/');
  }, [accessToken, refreshToken, idToken, persistTokens]);

  // Token refresh scheduler effect (runs after tokens / expiry change)
  useEffect(() => {
    if (loggedOut.current) return;
    if (!accessExpiresAt || !refreshToken) return;
    const now = Date.now();
    const msLeft = accessExpiresAt - now;
    if (msLeft <= 0) {
      logout();
      return;
    }
    const triggerIn = Math.max(msLeft - 30000, 5000);
    if (refreshTimer.current) window.clearTimeout(refreshTimer.current);
    refreshTimer.current = window.setTimeout(async () => {
      if (loggedOut.current) return;
      if (!refreshToken) return;
      const result = await refreshTokens(refreshToken);
      if (result.success && result.tokens && !loggedOut.current) {
        applyTokens(result.tokens);
      } else if (!loggedOut.current) {
        await logout();
      }
    }, triggerIn);
    return () => {
      if (refreshTimer.current) window.clearTimeout(refreshTimer.current);
    };
  }, [accessExpiresAt, refreshToken, applyTokens, logout]);

  // Restore persisted tokens on mount
  useEffect(() => {
    try {
      const raw = localStorage.getItem(PERSIST_KEY);
      if (raw) {
        const parsed: PersistedTokens = JSON.parse(raw);
        if (parsed.access_expires_at > Date.now() + 5000) {
          setAccessToken(parsed.access_token);
          setRefreshToken(parsed.refresh_token || null);
          setAccessExpiresAt(parsed.access_expires_at);
          setRefreshExpiresAt(parsed.refresh_expires_at || null);
          setIdToken(parsed.id_token);
          const u = parseUserFromTokens(parsed.access_token, parsed.id_token);
          setUser(u);
        } else {
          persistTokens(null);
        }
      }
    } catch (e) {
      console.warn('Failed to restore tokens', e);
    } finally {
      setIsLoading(false);
    }
  }, [persistTokens]);

  const startAuth = useCallback(async (mode: 'login' | 'register', redirectPath?: string) => {
    setError(null);
    const codeVerifier = generateCodeVerifier();
    const state = generateState();
    const nonce = generateNonce();
    const codeChallenge = await generateCodeChallenge(codeVerifier);

    storeTemp('pkce_verifier', codeVerifier);
    storeTemp('oidc_state', state);
    storeTemp('oidc_nonce', nonce);
    if (redirectPath) storeTemp('post_login_redirect', redirectPath);

    const params = new URLSearchParams({
      client_id: KC_CLIENT_ID,
      response_type: 'code',
      redirect_uri: buildRedirectUri(),
      scope: OIDC_SCOPE,
      code_challenge: codeChallenge,
      code_challenge_method: 'S256',
      state,
      nonce
    });
    if (mode === 'register') params.set('kc_action', 'register');
    window.location.href = AUTH_ENDPOINT + '?' + params.toString();
  }, []);

  const login = useCallback(async (redirectPath?: string) => startAuth('login', redirectPath), [startAuth]);
  const register = useCallback(async (redirectPath?: string) => startAuth('register', redirectPath), [startAuth]);

  const completeLogin = useCallback((tokens: { access_token: string; refresh_token?: string; expires_in: number; id_token?: string; refresh_expires_in?: number }) => {
    applyTokens(tokens);
    setIsLoading(false);
  }, [applyTokens]);

  const hasRole = (role: string) => !!user?.roles?.includes(role);

  useEffect(() => {
    const onStorage = (e: StorageEvent) => {
      if (e.key === PERSIST_KEY && e.newValue === null && !loggedOut.current) {
        // Another tab logged out
        loggedOut.current = true;
        if (refreshTimer.current) window.clearTimeout(refreshTimer.current);
        setUser(null);
        setAccessToken(null);
        setRefreshToken(null);
        setAccessExpiresAt(null);
        setRefreshExpiresAt(null);
        setIdToken(undefined);
      }
    };
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  return (
    <AuthContext.Provider value={{
      isLoading,
      user,
      isAuthenticated: !!accessToken && (!!accessExpiresAt && accessExpiresAt > Date.now()),
      accessToken,
      refreshToken,
      login,
      register,
      logout,
      completeLogin,
      hasRole,
      error
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}