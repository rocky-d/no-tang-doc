/**
 * Load Testing Script for no-tang-doc-core (Spring Boot REST API)
 * 
 * Tests Core Service REST API endpoints with OAuth 2.0 authentication.
 * Includes document upload/download, team management, and database operations.
 * 
 * Usage:
 *   k6 run --env ENVIRONMENT=development load-tests/k6/scripts/core-api.js
 *   k6 run --env SCENARIO=stress load-tests/k6/scripts/core-api.js
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { getClientCredentialsToken, createAuthHeaders } from '../utils/auth.js';

// Load configuration
const environments = JSON.parse(open('../config/environments.json'));
const thresholds = JSON.parse(open('../config/thresholds.json'));
const scenarios = JSON.parse(open('../config/scenarios.json'));

// Environment setup
const ENVIRONMENT = __ENV.ENVIRONMENT || __ENV.K6_ENV || 'production';
const env = environments[ENVIRONMENT];

// OAuth configuration
const KEYCLOAK_CLIENT_ID = __ENV.KEYCLOAK_CLIENT_ID || 'load-test-client';
const KEYCLOAK_CLIENT_SECRET = __ENV.KEYCLOAK_CLIENT_SECRET || '';
const KEYCLOAK_SCOPES = __ENV.KEYCLOAK_SCOPES || 'openid profile email';

// Test scenario
const SCENARIO = __ENV.SCENARIO || 'load';

// Custom metrics
const documentUploadDuration = new Trend('document_upload_duration');
const databaseQueryDuration = new Trend('database_query_duration');
const apiSuccessRate = new Rate('api_success_rate');
const documentUploadErrors = new Counter('document_upload_errors');

// Test configuration
export const options = {
  scenarios: {
    default: scenarios[SCENARIO],
  },
  thresholds: thresholds.core,
  tags: {
    service: 'core',
    environment: ENVIRONMENT,
    scenario: SCENARIO,
  },
};

/**
 * Setup - validate connectivity
 */
export function setup() {
  console.log(`\n🚀 Starting Core API Load Test`);
  console.log(`   Environment: ${ENVIRONMENT}`);
  console.log(`   Core URL: ${env.coreBaseUrl}`);
  console.log(`   Scenario: ${SCENARIO}\n`);

  // Validate OAuth
  const token = getClientCredentialsToken(
    env.issuerUrl,
    KEYCLOAK_CLIENT_ID,
    KEYCLOAK_CLIENT_SECRET,
    KEYCLOAK_SCOPES
  );

  if (!token) {
    throw new Error('Failed to obtain OAuth token');
  }

  // Validate Core API is accessible
  const headers = createAuthHeaders(token);
  const healthCheck = http.get(`${env.coreBaseUrl}/actuator/health`, { headers });
  
  if (healthCheck.status !== 200) {
    console.warn(`⚠️  Health check returned status ${healthCheck.status}`);
  } else {
    console.log('✅ Core API health check passed\n');
  }

  return {
    issuerUrl: env.issuerUrl,
    coreBaseUrl: env.coreBaseUrl,
    clientId: KEYCLOAK_CLIENT_ID,
    clientSecret: KEYCLOAK_CLIENT_SECRET,
    scopes: KEYCLOAK_SCOPES,
  };
}

/**
 * Main test function
 */
export default function(data) {
  // Get OAuth token
  const token = getClientCredentialsToken(
    data.issuerUrl,
    data.clientId,
    data.clientSecret,
    data.scopes
  );

  if (!token) {
    apiSuccessRate.add(0);
    sleep(1);
    return;
  }

  const headers = createAuthHeaders(token);

  // Test groups
  group('Teams API', () => {
    testGetTeams(data.coreBaseUrl, headers);
    testCreateTeam(data.coreBaseUrl, headers);
  });

  group('Documents API', () => {
    testGetDocuments(data.coreBaseUrl, headers);
    testUploadDocument(data.coreBaseUrl, headers);
  });

  group('Health & Metrics', () => {
    testActuatorHealth(data.coreBaseUrl, headers);
    testActuatorMetrics(data.coreBaseUrl, headers);
  });

  sleep(1);
}

/**
 * Test GET /api/v1/teams
 */
function testGetTeams(baseUrl, headers) {
  const start = Date.now();
  const res = http.get(`${baseUrl}/api/v1/teams`, {
    headers: headers,
    tags: { name: 'get_teams' },
  });
  const duration = Date.now() - start;

  const success = check(res, {
    'teams: status is 200': (r) => r.status === 200,
    'teams: has valid response': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body !== null;
      } catch (e) {
        return false;
      }
    },
    'teams: response time < 600ms': () => duration < 600,
  });

  databaseQueryDuration.add(duration, { operation: 'get_teams' });
  apiSuccessRate.add(success ? 1 : 0);
}

/**
 * Test POST /api/v1/teams
 */
function testCreateTeam(baseUrl, headers) {
  const teamName = `LoadTest-Team-${Date.now()}-${__VU}`;
  const payload = JSON.stringify({
    name: teamName,
    description: `Team created by load test VU ${__VU}`,
  });

  const start = Date.now();
  const res = http.post(`${baseUrl}/api/v1/teams`, payload, {
    headers: headers,
    tags: { name: 'create_team' },
  });
  const duration = Date.now() - start;

  const success = check(res, {
    'create-team: status is 201 or 200': (r) => r.status === 201 || r.status === 200,
    'create-team: response has id': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.id !== undefined || body.teamId !== undefined;
      } catch (e) {
        return false;
      }
    },
    'create-team: response time < 800ms': () => duration < 800,
  });

  databaseQueryDuration.add(duration, { operation: 'create_team' });
  apiSuccessRate.add(success ? 1 : 0);

  // Clean up - delete the created team (optional, comment out if not needed)
  if (success && res.status === 201) {
    try {
      const teamId = JSON.parse(res.body).id || JSON.parse(res.body).teamId;
      http.del(`${baseUrl}/api/v1/teams/${teamId}`, { headers });
    } catch (e) {
      // Ignore cleanup errors
    }
  }
}

/**
 * Test GET /api/v1/documents
 */
function testGetDocuments(baseUrl, headers) {
  const start = Date.now();
  const res = http.get(`${baseUrl}/api/v1/documents`, {
    headers: headers,
    tags: { name: 'get_documents' },
  });
  const duration = Date.now() - start;

  const success = check(res, {
    'documents: status is 200': (r) => r.status === 200,
    'documents: response time < 600ms': () => duration < 600,
  });

  databaseQueryDuration.add(duration, { operation: 'get_documents' });
  apiSuccessRate.add(success ? 1 : 0);
}

/**
 * Test POST /api/v1/documents (file upload)
 */
function testUploadDocument(baseUrl, headers) {
  // Create a small test file
  const testFileContent = `Load test document created at ${new Date().toISOString()} by VU ${__VU}`;
  const fileName = `loadtest-${Date.now()}-vu${__VU}.txt`;

  const fd = new FormData();
  fd.append('file', http.file(testFileContent, fileName, 'text/plain'));
  fd.append('title', `Load Test Doc ${Date.now()}`);
  fd.append('description', 'Automated load test document');

  const start = Date.now();
  const res = http.post(`${baseUrl}/api/v1/documents`, fd.body(), {
    headers: Object.assign({}, headers, {
      'Content-Type': `multipart/form-data; boundary=${fd.boundary}`,
    }),
    tags: { name: 'upload_document' },
  });
  const duration = Date.now() - start;

  const success = check(res, {
    'upload: status is 201 or 200': (r) => r.status === 201 || r.status === 200,
    'upload: response time < 2000ms': () => duration < 2000,
  });

  documentUploadDuration.add(duration);
  
  if (!success) {
    documentUploadErrors.add(1);
  }
  
  apiSuccessRate.add(success ? 1 : 0);

  // Clean up - delete the uploaded document (optional)
  if (success && res.status === 201) {
    try {
      const docId = JSON.parse(res.body).id || JSON.parse(res.body).documentId;
      http.del(`${baseUrl}/api/v1/documents/${docId}`, { headers });
    } catch (e) {
      // Ignore cleanup errors
    }
  }
}

/**
 * Test Spring Boot Actuator health endpoint
 */
function testActuatorHealth(baseUrl, headers) {
  const res = http.get(`${baseUrl}/actuator/health`, {
    headers: headers,
    tags: { name: 'actuator_health' },
  });

  check(res, {
    'health: status is 200': (r) => r.status === 200,
    'health: status is UP': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.status === 'UP';
      } catch (e) {
        return false;
      }
    },
  });
}

/**
 * Test Spring Boot Actuator metrics endpoint
 */
function testActuatorMetrics(baseUrl, headers) {
  const res = http.get(`${baseUrl}/actuator/metrics`, {
    headers: headers,
    tags: { name: 'actuator_metrics' },
  });

  check(res, {
    'metrics: status is 200': (r) => r.status === 200,
  });
}

/**
 * Teardown
 */
export function teardown(data) {
  console.log('\n✅ Core API Load Test Complete\n');
}
