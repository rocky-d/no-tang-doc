import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  generateCodeVerifier,
  generateCodeChallenge,
  generateState,
  generateNonce,
  storeTemp,
  readTemp,
  clearTemp,
} from '@/utils/pkce';

const b64urlRe = /^[A-Za-z0-9_-]+$/;

describe('PKCE utils', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('generates base64url verifier with default length', () => {
    const v = generateCodeVerifier();
    // default length=64 random bytes => base64 length ~= 4*ceil(64/3) - padding, after url-safe & trim
    expect(v.length).toBeGreaterThan(60);
    expect(b64urlRe.test(v)).toBe(true);
  });

  it('generates state and nonce as base64url strings', () => {
    const s = generateState();
    const n = generateNonce();
    expect(b64urlRe.test(s)).toBe(true);
    expect(b64urlRe.test(n)).toBe(true);
    expect(s).not.toEqual(n);
  });

  it('computes S256 code challenge for a given verifier', async () => {
    const verifier = 'test_verifier_123';
    const challenge = await generateCodeChallenge(verifier);
    // 32-byte SHA-256 digest -> base64url length 43 after trimming '='
    expect(challenge.length).toBe(43);
    expect(b64urlRe.test(challenge)).toBe(true);
  });

  it('stores, reads and clears temporary values', () => {
    storeTemp('k1', 'v1');
    expect(readTemp('k1')).toBe('v1');
    clearTemp(['k1']);
    expect(readTemp('k1')).toBeNull();
  });
});

