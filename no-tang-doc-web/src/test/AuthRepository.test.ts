// filepath: d:\Code\NoTangDoc\no-tang-doc-web\src\test\AuthRepository.test.ts
import { describe, it, expect, vi } from 'vitest';
import { HttpAuthRepository, setAuthRepository } from '../repositories/AuthRepository';

// Minimal fetch mock helper
function mockFetchOnce(response: any, ok = true, status = 200) {
  (globalThis as any).fetch = vi.fn().mockResolvedValue({
    ok,
    status,
    json: vi.fn().mockResolvedValue(response),
  });
}

describe('AuthRepository', () => {
  it('exchangeAuthorizationCode returns tokens on success', async () => {
    const repo = new HttpAuthRepository();
    mockFetchOnce({ access_token: 'abc', refresh_token: 'ref', expires_in: 100, id_token: 'id' });
    const res = await repo.exchangeAuthorizationCode({ code: 'c', codeVerifier: 'v', redirectUri: 'http://x', nonce: 'n' });
    expect(res.success).toBe(true);
    expect(res.tokens?.access_token).toBe('abc');
  });

  it('exchangeAuthorizationCode handles error payload', async () => {
    const repo = new HttpAuthRepository();
    mockFetchOnce({ error: 'bad_code' });
    const res = await repo.exchangeAuthorizationCode({ code: 'bad', codeVerifier: 'v', redirectUri: 'http://x', nonce: 'n' });
    expect(res.success).toBe(false);
    expect(res.error).toMatch(/bad_code/);
  });

  it('refreshTokens returns updated tokens', async () => {
    const repo = new HttpAuthRepository();
    mockFetchOnce({ access_token: 'newAccess', refresh_token: 'newRefresh', expires_in: 200 });
    const res = await repo.refreshTokens('oldRefresh');
    expect(res.success).toBe(true);
    expect(res.tokens?.access_token).toBe('newAccess');
    expect(res.tokens?.refresh_token).toBe('newRefresh');
  });

  it('revokeSession succeeds with success flag', async () => {
    const repo = new HttpAuthRepository();
    mockFetchOnce({ success: true });
    const ok = await repo.revokeSession('at', 'rt', 'id');
    expect(ok).toBe(true);
  });

  it('setAuthRepository can swap implementation', async () => {
    class MockAuthRepo extends HttpAuthRepository {
      async exchangeAuthorizationCode() { return { success: true, tokens: { access_token: 'mock', expires_in: 10 } }; }
      async refreshTokens() { return { success: true, tokens: { access_token: 'mock2', expires_in: 10 } }; }
    }
    const mock = new MockAuthRepo();
    setAuthRepository(mock);
    const r1 = await mock.exchangeAuthorizationCode({ code: 'x', codeVerifier: 'y', redirectUri: 'z', nonce: 'n' });
    expect(r1.tokens?.access_token).toBe('mock');
  });
});

