/**
 * Authentication utilities for k6 load tests
 * Handles Keycloak OAuth 2.0 token acquisition
 */

import http from 'k6/http';
import { check } from 'k6';

/**
 * Get OAuth token using client credentials flow
 * Used for service-to-service authentication in load tests
 * 
 * @param {string} issuerUrl - Keycloak realm issuer URL (e.g., https://auth.ntdoc.site/realms/ntdoc)
 * @param {string} clientId - OAuth client ID
 * @param {string} clientSecret - OAuth client secret
 * @param {string} scopes - Space-separated scopes (default: "openid profile email")
 * @returns {string|null} Access token or null if authentication fails
 */
export function getClientCredentialsToken(issuerUrl, clientId, clientSecret, scopes = 'openid profile email') {
  const tokenEndpoint = `${issuerUrl}/protocol/openid-connect/token`;
  
  const payload = {
    grant_type: 'client_credentials',
    client_id: clientId,
    client_secret: clientSecret,
    scope: scopes,
  };

  const params = {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    tags: { name: 'OAuth_ClientCredentials' },
  };

  const response = http.post(tokenEndpoint, payload, params);

  const success = check(response, {
    'OAuth: status is 200': (r) => r.status === 200,
    'OAuth: has access_token': (r) => r.json('access_token') !== undefined,
  });

  if (!success) {
    console.error(`OAuth authentication failed: ${response.status} ${response.body}`);
    return null;
  }

  return response.json('access_token');
}

/**
 * Get OAuth token using password grant flow (Resource Owner Password Credentials)
 * Used for user-impersonating tests
 * 
 * @param {string} issuerUrl - Keycloak realm issuer URL
 * @param {string} clientId - OAuth client ID
 * @param {string} clientSecret - OAuth client secret (optional for public clients)
 * @param {string} username - User username
 * @param {string} password - User password
 * @param {string} scopes - Space-separated scopes
 * @returns {string|null} Access token or null if authentication fails
 */
export function getPasswordToken(issuerUrl, clientId, clientSecret, username, password, scopes = 'openid profile email') {
  const tokenEndpoint = `${issuerUrl}/protocol/openid-connect/token`;
  
  const payload = {
    grant_type: 'password',
    client_id: clientId,
    username: username,
    password: password,
    scope: scopes,
  };

  // Add client_secret if provided (for confidential clients)
  if (clientSecret) {
    payload.client_secret = clientSecret;
  }

  const params = {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    tags: { name: 'OAuth_Password' },
  };

  const response = http.post(tokenEndpoint, payload, params);

  const success = check(response, {
    'OAuth: status is 200': (r) => r.status === 200,
    'OAuth: has access_token': (r) => r.json('access_token') !== undefined,
  });

  if (!success) {
    console.error(`OAuth password authentication failed: ${response.status} ${response.body}`);
    return null;
  }

  return response.json('access_token');
}

/**
 * Create authorization header with Bearer token
 * 
 * @param {string} token - Access token
 * @returns {object} Headers object with Authorization field
 */
export function createAuthHeaders(token) {
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
}

/**
 * Validate token by checking its expiration
 * Note: This is a basic check, doesn't verify signature
 * 
 * @param {string} token - JWT access token
 * @returns {boolean} True if token appears valid and not expired
 */
export function isTokenValid(token) {
  if (!token || typeof token !== 'string') {
    return false;
  }

  try {
    const parts = token.split('.');
    if (parts.length !== 3) {
      return false;
    }

    // Decode payload (base64url)
    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
    
    // Check expiration (exp is in seconds, Date.now() is in milliseconds)
    if (payload.exp && payload.exp * 1000 < Date.now()) {
      return false;
    }

    return true;
  } catch (error) {
    console.error(`Token validation failed: ${error}`);
    return false;
  }
}

/**
 * Get token with automatic retry on failure
 * 
 * @param {Function} tokenGetter - Function that returns a token
 * @param {number} maxRetries - Maximum number of retry attempts (default: 3)
 * @param {number} retryDelay - Delay between retries in ms (default: 1000)
 * @returns {string|null} Access token or null if all retries fail
 */
export function getTokenWithRetry(tokenGetter, maxRetries = 3, retryDelay = 1000) {
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    const token = tokenGetter();
    
    if (token && isTokenValid(token)) {
      return token;
    }

    if (attempt < maxRetries) {
      console.warn(`Token acquisition failed (attempt ${attempt}/${maxRetries}), retrying in ${retryDelay}ms...`);
      sleep(retryDelay / 1000); // k6 sleep expects seconds
    }
  }

  console.error(`Failed to acquire valid token after ${maxRetries} attempts`);
  return null;
}

// Helper function for sleep (k6's sleep is imported from k6)
function sleep(duration) {
  // This will be overridden by k6's sleep when imported
  const start = Date.now();
  while (Date.now() - start < duration * 1000) {
    // Busy wait
  }
}
