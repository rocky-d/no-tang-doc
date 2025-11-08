// Enhanced PKCE & OIDC helper utilities
// Includes: code_verifier (base64url), code_challenge(S256), state, nonce generation

function base64UrlEncode(bytes: Uint8Array) {
  const str = btoa(String.fromCharCode(...Array.from(bytes)));
  return str.replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
}

function randomBytes(length: number) {
  const arr = new Uint8Array(length);
  crypto.getRandomValues(arr);
  return arr;
}

export function generateCodeVerifier(length: number = 64) {
  // RFC 7636: between 43 and 128 characters (un-padded base64url)
  return base64UrlEncode(randomBytes(length));
}

export async function generateCodeChallenge(codeVerifier: string) {
  const data = new TextEncoder().encode(codeVerifier);
  const digest = await crypto.subtle.digest('SHA-256', data);
  return base64UrlEncode(new Uint8Array(digest));
}

export function generateState() {
  return base64UrlEncode(randomBytes(16));
}

export function generateNonce() {
  return base64UrlEncode(randomBytes(16));
}

export function storeTemp(key: string, value: string) {
  sessionStorage.setItem(key, value);
}

export function readTemp(key: string) {
  return sessionStorage.getItem(key);
}

export function clearTemp(keys: string[]) {
  keys.forEach(k => sessionStorage.removeItem(k));
}
