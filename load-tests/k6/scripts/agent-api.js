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
const mcpToolCallDuration = new Trend('mcp_tool_call_duration');
const authSuccessRate = new Rate('auth_success_rate');
const apiErrorRate = new Rate('api_error_rate');
const mcpToolErrors = new Counter('mcp_tool_errors');

// Test configuration
export const options = {
  // Use scenario from config
  scenarios: {
    default: scenarios[SCENARIO],
  },
  
  // Thresholds for agent service
  thresholds: thresholds.agent,
  
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
 */
export default function(data) {
  // Get OAuth token (in real scenario, tokens would be cached/reused)
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
  const headers = createAuthHeaders(token);

  // Test groups for organizing metrics
  group('MCP Tools - Teams', () => {
    testGetTeams(data.agentBaseUrl, headers);
    testGetTeamMembers(data.agentBaseUrl, headers);
  });

  group('MCP Tools - Documents', () => {
    testGetDocuments(data.agentBaseUrl, headers);
  });

  group('MCP Tools - Logs', () => {
    testGetLogs(data.agentBaseUrl, headers);
  });

  group('Health Check', () => {
    testHealthCheck(data.agentBaseUrl);
  });

  // Realistic think time between iterations
  sleep(1);
}

/**
 * Test get-teams MCP tool
 */
function testGetTeams(baseUrl, headers) {
  const start = Date.now();
  const res = http.get(`${baseUrl}/api/v1/teams`, {
    headers: headers,
    tags: { name: 'get_teams' },
  });
  const duration = Date.now() - start;

  const success = check(res, {
    'get-teams: status is 200': (r) => r.status === 200,
    'get-teams: response has data': (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body) || body.data !== undefined;
      } catch (e) {
        return false;
      }
    },
    'get-teams: response time < 500ms': () => duration < 500,
  });

  mcpToolCallDuration.add(duration, { tool: 'get_teams' });
  
  if (!success) {
    mcpToolErrors.add(1, { tool: 'get_teams' });
    apiErrorRate.add(1);
  } else {
    apiErrorRate.add(0);
  }
}

/**
 * Test get-team-members MCP tool
 */
function testGetTeamMembers(baseUrl, headers) {
  // Use a test team ID (should be adjusted based on your test data)
  const testTeamId = __ENV.TEST_TEAM_ID || '1';
  
  const start = Date.now();
  const res = http.get(`${baseUrl}/api/v1/teams/${testTeamId}/members`, {
    headers: headers,
    tags: { name: 'get_team_members' },
  });
  const duration = Date.now() - start;

  const success = check(res, {
    'get-team-members: status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'get-team-members: response time < 500ms': () => duration < 500,
  });

  mcpToolCallDuration.add(duration, { tool: 'get_team_members' });
  
  if (!success && res.status !== 404) {
    mcpToolErrors.add(1, { tool: 'get_team_members' });
    apiErrorRate.add(1);
  } else {
    apiErrorRate.add(0);
  }
}

/**
 * Test get-documents MCP tool
 */
function testGetDocuments(baseUrl, headers) {
  const start = Date.now();
  const res = http.get(`${baseUrl}/api/v1/documents`, {
    headers: headers,
    tags: { name: 'get_documents' },
  });
  const duration = Date.now() - start;

  const success = check(res, {
    'get-documents: status is 200': (r) => r.status === 200,
    'get-documents: response has data': (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body) || body.data !== undefined;
      } catch (e) {
        return false;
      }
    },
    'get-documents: response time < 600ms': () => duration < 600,
  });

  mcpToolCallDuration.add(duration, { tool: 'get_documents' });
  
  if (!success) {
    mcpToolErrors.add(1, { tool: 'get_documents' });
    apiErrorRate.add(1);
  } else {
    apiErrorRate.add(0);
  }
}

/**
 * Test get-logs MCP tool
 */
function testGetLogs(baseUrl, headers) {
  const start = Date.now();
  const res = http.get(`${baseUrl}/api/v1/logs`, {
    headers: headers,
    tags: { name: 'get_logs' },
  });
  const duration = Date.now() - start;

  const success = check(res, {
    'get-logs: status is 200': (r) => r.status === 200,
    'get-logs: response time < 500ms': () => duration < 500,
  });

  mcpToolCallDuration.add(duration, { tool: 'get_logs' });
  
  if (!success) {
    mcpToolErrors.add(1, { tool: 'get_logs' });
    apiErrorRate.add(1);
  } else {
    apiErrorRate.add(0);
  }
}

/**
 * Test health check endpoint (if available)
 */
function testHealthCheck(baseUrl) {
  const res = http.get(`${baseUrl}/health`, {
    tags: { name: 'health_check' },
  });

  check(res, {
    'health-check: status is 200 or 404': (r) => r.status === 200 || r.status === 404,
  });
}

/**
 * Teardown function - runs once after test completes
 */
export function teardown(data) {
  console.log('\n✅ Agent Load Test Complete\n');
}
