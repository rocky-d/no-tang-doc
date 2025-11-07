/**
 * Load Testing Script for no-tang-doc-agent (MCP Server)
 * 
 * Tests Agent Service API endpoints with OAuth 2.0 authentication via Keycloak.
 * Focuses on MCP tool performance and authentication flow.
 * 
 * Usage:
 *   # Local development
 *   k6 run --env ENVIRONMENT=development load-tests/k6/scripts/agent-api.js
 * 
 *   # Production testing
 *   K6_ENV=production k6 run load-tests/k6/scripts/agent-api.js
 * 
 *   # With custom scenario
 *   k6 run --env SCENARIO=smoke load-tests/k6/scripts/agent-api.js
 * 
 *   # CI/CD (environment variables set in workflow)
 *   k6 run load-tests/k6/scripts/agent-api.js
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { getClientCredentialsToken, createAuthHeaders } from '../utils/auth.js';

// Load configuration files
const environments = JSON.parse(open('../config/environments.json'));
const thresholds = JSON.parse(open('../config/thresholds.json'));
const scenarios = JSON.parse(open('../config/scenarios.json'));

// Determine environment from env vars (default: production)
const ENVIRONMENT = __ENV.ENVIRONMENT || __ENV.K6_ENV || 'production';
const env = environments[ENVIRONMENT];

// OAuth configuration from environment variables
const KEYCLOAK_CLIENT_ID = __ENV.KEYCLOAK_CLIENT_ID || 'load-test-client';
const KEYCLOAK_CLIENT_SECRET = __ENV.KEYCLOAK_CLIENT_SECRET || '';
const KEYCLOAK_SCOPES = __ENV.KEYCLOAK_SCOPES || 'openid profile email mcp-user';

// Test scenario (default: load)
const SCENARIO = __ENV.SCENARIO || 'load';

// Custom metrics
const connectivityDuration = new Trend('connectivity_duration');
const authSuccessRate = new Rate('auth_success_rate');
const connectivitySuccessRate = new Rate('connectivity_success_rate');

// Test configuration
export const options = {
  // Use scenario from config
  scenarios: {
    default: scenarios[SCENARIO],
  },
  
  // Thresholds for agent service (simplified)
  thresholds: {
    'http_req_duration': ['p(95)<1000'],
    'http_req_failed': ['rate<0.1'],
    'checks': ['rate>0.95'],
    'auth_success_rate': ['rate>0.99'],
    'connectivity_success_rate': ['rate>0.95'],
  },
  
  // Tags for filtering metrics
  tags: {
    service: 'agent',
    environment: ENVIRONMENT,
    scenario: SCENARIO,
  },
};

/**
 * Setup function - runs once before test starts
 * Validates configuration and connectivity
 */
export function setup() {
  console.log(`\n🚀 Starting Agent Load Test`);
  console.log(`   Environment: ${ENVIRONMENT}`);
  console.log(`   Agent URL: ${env.agentBaseUrl}`);
  console.log(`   Keycloak: ${env.issuerUrl}`);
  console.log(`   Scenario: ${SCENARIO}`);
  console.log(`   Duration: ${scenarios[SCENARIO].duration || 'varies'}\n`);

  // Get initial token to validate auth
  const token = getClientCredentialsToken(
    env.issuerUrl,
    KEYCLOAK_CLIENT_ID,
    KEYCLOAK_CLIENT_SECRET,
    KEYCLOAK_SCOPES
  );

  if (!token) {
    throw new Error('Failed to obtain OAuth token during setup. Check credentials.');
  }

  console.log('✅ OAuth authentication validated\n');

  return {
    issuerUrl: env.issuerUrl,
    agentBaseUrl: env.agentBaseUrl,
    clientId: KEYCLOAK_CLIENT_ID,
    clientSecret: KEYCLOAK_CLIENT_SECRET,
    scopes: KEYCLOAK_SCOPES,
  };
}

/**
 * Main test function - runs for each VU iteration
 * 
 * IMPORTANT: Agent is an MCP (Model Context Protocol) server, not a REST API.
 * This test only validates OAuth authentication and basic connectivity.
 * Testing actual MCP protocol (JSON-RPC 2.0 over SSE/HTTP) requires specialized MCP clients.
 * 
 * MCP servers expose functionality through:
 * - Tools: Callable functions (e.g., get-teams, get-documents)
 * - Resources: Data sources with URIs
 * - Prompts: Templated interactions
 * 
 * These are accessed via JSON-RPC protocol at the /mcp endpoint, not via REST API.
 */
export default function(data) {
  // Test OAuth authentication
  const token = getClientCredentialsToken(
    data.issuerUrl,
    data.clientId,
    data.clientSecret,
    data.scopes
  );

  if (!token) {
    authSuccessRate.add(0);
    sleep(1);
    return;
  }

  authSuccessRate.add(1);

  // Basic connectivity test to MCP endpoint
  group('OAuth & MCP Connectivity', () => {
    testMcpEndpointConnectivity(data.agentBaseUrl, token);
  });

  sleep(1);
}

/**
 * Test basic MCP endpoint connectivity
 * 
 * Note: Agent MCP server listens on /mcp endpoint and expects JSON-RPC 2.0 protocol.
 * Without proper MCP handshake, the server will reject requests, which is expected behavior.
 * This test only validates that:
 * 1. OAuth token is accepted (401/403 = auth issue, 4xx = protocol issue = OK)
 * 2. Server responds (not timeout/network error)
 * 3. Response time is reasonable
 */
function testMcpEndpointConnectivity(baseUrl, token) {
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  const start = Date.now();
  const res = http.get(`${baseUrl}/mcp`, {
    headers: headers,
    tags: { name: 'mcp_endpoint_connectivity' },
  });
  const duration = Date.now() - start;

  // MCP server expects POST with JSON-RPC, so GET may return:
  // - 405 Method Not Allowed (expected)
  // - 400 Bad Request (expected - not JSON-RPC)
  // - 401/403 (authentication issue - not expected)
  // - 200/404 (depends on server implementation)
  const success = check(res, {
    'mcp-connectivity: server responds': (r) => r.status !== 0,
    'mcp-connectivity: not auth error': (r) => r.status !== 401 && r.status !== 403,
    'mcp-connectivity: response time < 1000ms': () => duration < 1000,
  });

  connectivityDuration.add(duration);
  connectivitySuccessRate.add(success ? 1 : 0);
}

/**
 * Teardown function - runs once after test completes
 */
export function teardown(data) {
  console.log('\n✅ Agent Load Test Complete');
  console.log('   Note: This test only validates OAuth + connectivity.');
  console.log('   Full MCP protocol testing requires specialized MCP clients.\n');
}
