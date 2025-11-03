/**
 * Load Testing Script for no-tang-doc-web (React Frontend)
 * 
 * Tests Web Frontend static resources and SPA routing performance.
 * Simulates user browsing behavior across the application.
 * 
 * Usage:
 *   k6 run --env ENVIRONMENT=development load-tests/k6/scripts/web-frontend.js
 *   k6 run --env SCENARIO=spike load-tests/k6/scripts/web-frontend.js
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Load configuration
const environments = JSON.parse(open('../config/environments.json'));
const thresholds = JSON.parse(open('../config/thresholds.json'));
const scenarios = JSON.parse(open('../config/scenarios.json'));

// Environment setup
const ENVIRONMENT = __ENV.ENVIRONMENT || __ENV.K6_ENV || 'production';
const env = environments[ENVIRONMENT];

// Test scenario
const SCENARIO = __ENV.SCENARIO || 'load';

// Custom metrics
const staticResourceDuration = new Trend('static_resource_duration');
const pageLoadDuration = new Trend('page_load_duration');
const resourceSuccessRate = new Rate('resource_success_rate');

// Test configuration
export const options = {
  scenarios: {
    default: scenarios[SCENARIO],
  },
  thresholds: thresholds.web,
  tags: {
    service: 'web',
    environment: ENVIRONMENT,
    scenario: SCENARIO,
  },
};

/**
 * Setup
 */
export function setup() {
  console.log(`\n🚀 Starting Web Frontend Load Test`);
  console.log(`   Environment: ${ENVIRONMENT}`);
  console.log(`   Web URL: ${env.webBaseUrl}`);
  console.log(`   Scenario: ${SCENARIO}\n`);

  // Check if web is accessible
  const res = http.get(env.webBaseUrl);
  if (res.status !== 200) {
    console.warn(`⚠️  Web frontend returned status ${res.status}`);
  } else {
    console.log('✅ Web frontend is accessible\n');
  }

  return {
    webBaseUrl: env.webBaseUrl,
  };
}

/**
 * Main test function - simulate user browsing
 */
export default function(data) {
  group('Homepage Load', () => {
    testHomepage(data.webBaseUrl);
  });

  group('Static Resources', () => {
    testStaticResources(data.webBaseUrl);
  });

  group('SPA Navigation', () => {
    testDocumentsPage(data.webBaseUrl);
    testTeamsPage(data.webBaseUrl);
    testUploadPage(data.webBaseUrl);
  });

  // Simulate user reading time
  sleep(2);
}

/**
 * Test homepage load
 */
function testHomepage(baseUrl) {
  const start = Date.now();
  const res = http.get(baseUrl, {
    tags: { name: 'homepage' },
  });
  const duration = Date.now() - start;

  const success = check(res, {
    'homepage: status is 200': (r) => r.status === 200,
    'homepage: is HTML': (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('html'),
    'homepage: has app root': (r) => r.body.includes('id="root"') || r.body.includes('id="app"'),
    'homepage: load time < 200ms': () => duration < 200,
  });

  pageLoadDuration.add(duration, { page: 'homepage' });
  resourceSuccessRate.add(success ? 1 : 0);
}

/**
 * Test static resources (JS, CSS)
 */
function testStaticResources(baseUrl) {
  // Common static resource patterns for Vite-built apps
  const resources = [
    '/assets/index.js',
    '/assets/index.css',
    '/assets/vendor.js',
  ];

  resources.forEach((resource) => {
    const start = Date.now();
    const res = http.get(`${baseUrl}${resource}`, {
      tags: { name: 'static_resource' },
    });
    const duration = Date.now() - start;

    // 404 is acceptable for some resources that may not exist
    const success = check(res, {
      [`static: ${resource} status is 200 or 404`]: (r) => r.status === 200 || r.status === 404,
      [`static: ${resource} load time < 100ms`]: () => duration < 100,
    });

    if (res.status === 200) {
      staticResourceDuration.add(duration, { resource: resource.split('/').pop() });
      resourceSuccessRate.add(success ? 1 : 0);
    }
  });
}

/**
 * Test documents page (SPA route)
 */
function testDocumentsPage(baseUrl) {
  const start = Date.now();
  const res = http.get(`${baseUrl}/documents`, {
    tags: { name: 'documents_page' },
  });
  const duration = Date.now() - start;

  const success = check(res, {
    'documents: status is 200': (r) => r.status === 200,
    'documents: load time < 200ms': () => duration < 200,
  });

  pageLoadDuration.add(duration, { page: 'documents' });
  resourceSuccessRate.add(success ? 1 : 0);
}

/**
 * Test teams page (SPA route)
 */
function testTeamsPage(baseUrl) {
  const start = Date.now();
  const res = http.get(`${baseUrl}/teams`, {
    tags: { name: 'teams_page' },
  });
  const duration = Date.now() - start;

  const success = check(res, {
    'teams: status is 200': (r) => r.status === 200,
    'teams: load time < 200ms': () => duration < 200,
  });

  pageLoadDuration.add(duration, { page: 'teams' });
  resourceSuccessRate.add(success ? 1 : 0);
}

/**
 * Test upload page (SPA route)
 */
function testUploadPage(baseUrl) {
  const start = Date.now();
  const res = http.get(`${baseUrl}/upload`, {
    tags: { name: 'upload_page' },
  });
  const duration = Date.now() - start;

  const success = check(res, {
    'upload: status is 200': (r) => r.status === 200,
    'upload: load time < 200ms': () => duration < 200,
  });

  pageLoadDuration.add(duration, { page: 'upload' });
  resourceSuccessRate.add(success ? 1 : 0);
}

/**
 * Teardown
 */
export function teardown(data) {
  console.log('\n✅ Web Frontend Load Test Complete\n');
}
