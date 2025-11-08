import { describe, it, expect, beforeEach, vi } from 'vitest';
import { http, request as rawRequest } from '@/utils/request';

declare global {
  // extend type for mocked fetch
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace NodeJS { interface Global {} }
}

const PERSIST_KEY = 'auth_tokens_v1';

function mockFetchJsonOnce(data: any, init: Partial<Response> = {}) {
  const headers = new Headers({ 'content-type': 'application/json', ...(init.headers as any) });
  (global.fetch as any).mockResolvedValueOnce(new Response(JSON.stringify(data), { status: init.status ?? 200, headers }));
}

function mockFetchTextOnce(text: string, init: Partial<Response> = {}) {
  const headers = new Headers({ 'content-type': 'text/plain', ...(init.headers as any) });
  (global.fetch as any).mockResolvedValueOnce(new Response(text, { status: init.status ?? 200, headers }));
}

function setupAbortableFetch() {
  (global.fetch as any).mockImplementation((input: RequestInfo | URL, init?: RequestInit) => {
    return new Promise((_resolve, reject) => {
      const signal = init?.signal as AbortSignal | undefined;
      if (signal) {
        signal.addEventListener('abort', () => {
          reject(new DOMException('Aborted', 'AbortError'));
        });
      }
      // never resolve; abort will reject
    });
  });
}

describe('http/request helper', () => {
  beforeEach(() => {
    vi.useRealTimers();
    (global.fetch as any) = vi.fn();
    localStorage.clear();
    sessionStorage.clear();
  });

  it('attaches Authorization when a valid token is stored', async () => {
    const tokens = { access_token: 'abc123', access_expires_at: Date.now() + 60_000 };
    localStorage.setItem(PERSIST_KEY, JSON.stringify(tokens));

    mockFetchJsonOnce({ ok: true });

    const res = await http.get<{ ok: boolean }>('/any');

    expect(res.ok).toBe(true);
    const lastCall = (global.fetch as any).mock.calls.at(-1);
    const init = lastCall?.[1] as RequestInit;
    expect(init?.headers).toMatchObject({ Authorization: 'Bearer abc123' });
  });

  it('omits Authorization when withAuth=false', async () => {
    mockFetchJsonOnce({ ok: 1 });
    await rawRequest('/x', { withAuth: false });
    const init = (global.fetch as any).mock.calls.at(-1)?.[1] as RequestInit;
    expect(init?.headers).not.toHaveProperty('Authorization');
  });

  it('parses JSON and text responses based on content-type', async () => {
    mockFetchJsonOnce({ a: 1 });
    const j = await http.get<any>('/json');
    expect(j.a).toBe(1);

    mockFetchTextOnce('hello');
    const t = await http.get<string>('/text');
    expect(t).toBe('hello');
  });

  it('throws on non-2xx responses including best-effort text', async () => {
    const headers = new Headers({ 'content-type': 'text/plain' });
    (global.fetch as any).mockResolvedValueOnce(new Response('Bad', { status: 400, headers }));
    await expect(http.get('/bad')).rejects.toThrow(/HTTP 400: Bad/);
  });

  it('aborts after timeout and rejects', async () => {
    vi.useFakeTimers();
    setupAbortableFetch();
    const p = rawRequest('/slow', { timeoutMs: 10 });
    vi.advanceTimersByTime(11);
    await expect(p).rejects.toBeInstanceOf(DOMException);
  });
});

