# Load Testing for no-tang-doc

Comprehensive load testing infrastructure for the no-tang-doc microservices ecosystem using [k6](https://k6.io/) by Grafana Labs.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Test Scripts](#test-scripts)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [CI/CD Integration](#cicd-integration)
- [Metrics & Thresholds](#metrics--thresholds)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)

---

## Overview

This load testing suite provides:

- ✅ **Service Coverage**: Tests for Agent (MCP), Core (REST API), and Web (Frontend)
- ✅ **Multiple Scenarios**: Smoke, Load, Stress, Spike, Soak, and CI tests
- ✅ **OAuth 2.0 Authentication**: Full Keycloak integration
- ✅ **CI/CD Ready**: GitHub Actions workflows for manual and scheduled testing
- ✅ **Flexible Configuration**: JSON-based thresholds and scenarios
- ✅ **Comprehensive Metrics**: Custom metrics for business-specific KPIs

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│           GitHub Actions / Local Environment            │
│  ┌───────────────────────────────────────────────────┐  │
│  │           k6 Load Test Execution                  │  │
│  │  ┌──────────────┐  ┌──────────────┐             │  │
│  │  │  agent-api   │  │   core-api   │             │  │
│  │  │  .js         │  │   .js        │             │  │
│  │  └──────┬───────┘  └──────┬───────┘             │  │
│  │         │                  │                      │  │
│  │         │  ┌───────────────▼──────────┐         │  │
│  │         │  │  web-frontend.js         │         │  │
│  │         │  └──────────────────────────┘         │  │
│  │         │                                         │  │
│  │  ┌──────▼─────────────────────────────────────┐ │  │
│  │  │  Shared Utilities & Configuration        │ │  │
│  │  │  - auth.js (OAuth helper)                │ │  │
│  │  │  - thresholds.json (Performance SLAs)   │ │  │
│  │  │  - scenarios.json (Test patterns)       │ │  │
│  │  │  - environments.json (Target URLs)      │ │  │
│  │  └─────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────────┘
                       │
          ┌────────────┼────────────┐
          │            │            │
     ┌────▼────┐  ┌────▼────┐  ┌────▼────┐
     │  Agent  │  │  Core   │  │   Web   │
     │ Service │  │ Service │  │ Service │
     └─────────┘  └─────────┘  └─────────┘
         │            │            │
         └────────────┼────────────┘
                      │
              ┌───────▼───────┐
              │   Keycloak    │ ← OAuth 2.0 Provider
              │   auth.ntdoc  │
              └───────────────┘
```

---

## Quick Start

### Prerequisites

- **k6**: Install from https://k6.io/docs/get-started/installation/
  ```powershell
  # Windows (Chocolatey)
  choco install k6

  # Windows (Winget)
  winget install k6

  # macOS
  brew install k6

  # Linux
  sudo gpg -k
  sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
  echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
  sudo apt-get update
  sudo apt-get install k6
  ```

- **OAuth Credentials**: See [KEYCLOAK_SETUP.md](./KEYCLOAK_SETUP.md) for configuration

### Run Your First Test

1. **Set environment variables**:
   ```powershell
   # PowerShell
   $env:KEYCLOAK_CLIENT_ID = "load-test-client"
   $env:KEYCLOAK_CLIENT_SECRET = "your-client-secret"
   ```

2. **Run a smoke test** (minimal load):
   ```powershell
   k6 run --env SCENARIO=smoke load-tests/k6/scripts/agent-api.js
   ```

3. **View results** in the terminal output

---

## Test Scripts

### Agent Service (`agent-api.js`)

Tests the MCP Server API endpoints:
- OAuth 2.0 client credentials flow
- MCP tools: `get-teams`, `get-documents`, `get-logs`
- Custom metrics: `mcp_tool_call_duration`, `auth_success_rate`

**Key Endpoints**:
- `GET /api/v1/teams`
- `GET /api/v1/teams/{id}/members`
- `GET /api/v1/documents`
- `GET /api/v1/logs`

### Core Service (`core-api.js`)

Tests the Spring Boot REST API:
- CRUD operations for teams and documents
- File upload performance
- Spring Actuator health checks
- Database query performance

**Key Endpoints**:
- `GET /api/v1/teams`
- `POST /api/v1/teams`
- `GET /api/v1/documents`
- `POST /api/v1/documents` (multipart/form-data)
- `GET /actuator/health`

### Web Frontend (`web-frontend.js`)

Tests the React SPA:
- Homepage load time
- Static resource (JS/CSS) loading
- SPA route navigation
- Nginx reverse proxy performance

**Key Routes**:
- `/` (homepage)
- `/documents`
- `/teams`
- `/upload`

---

## Configuration

### Environments (`k6/config/environments.json`)

Define target URLs for different environments:

```json
{
  "production": {
    "issuerUrl": "https://auth.ntdoc.site/realms/ntdoc",
    "agentBaseUrl": "https://agent.ntdoc.site",
    "coreBaseUrl": "https://api.ntdoc.site",
    "webBaseUrl": "https://ntdoc.site"
  },
  "development": {
    "issuerUrl": "http://auth.local:8080/realms/ntdoc",
    "agentBaseUrl": "http://localhost:8002",
    "coreBaseUrl": "http://localhost:8070",
    "webBaseUrl": "http://localhost:3000"
  }
}
```

### Scenarios (`k6/config/scenarios.json`)

Predefined test patterns:

| Scenario | Description | Duration | VUs |
|----------|-------------|----------|-----|
| **smoke** | Minimal load validation | 1m | 1 |
| **load** | Normal expected traffic | 9m | 10 (ramp) |
| **stress** | Above-normal load to find limits | 16m | 40 (ramp) |
| **spike** | Sudden traffic surge | 6m | 50 (spike) |
| **soak** | Sustained load over time | 30m | 15 |
| **ci** | Quick validation for PRs | 3m | 5 |

### Thresholds (`k6/config/thresholds.json`)

Performance SLAs for each service:

**Agent Service:**
- `p(95) < 500ms` - 95th percentile response time
- `p(99) < 800ms` - 99th percentile response time
- `http_req_failed rate < 1%` - Error rate
- `checks rate > 95%` - Validation pass rate

**Core Service:**
- `p(95) < 600ms`
- `document_upload_duration p(95) < 2000ms`

**Web Frontend:**
- `p(95) < 200ms` - Static resources
- `http_req_failed rate < 0.5%`

---

## Running Tests

### Local Execution

#### Basic Run

```powershell
# Default (production + load scenario)
k6 run load-tests/k6/scripts/agent-api.js

# Specify environment
k6 run --env ENVIRONMENT=development load-tests/k6/scripts/agent-api.js

# Specify scenario
k6 run --env SCENARIO=stress load-tests/k6/scripts/core-api.js

# Both
k6 run --env ENVIRONMENT=production --env SCENARIO=smoke load-tests/k6/scripts/web-frontend.js
```

#### With OAuth Credentials

```powershell
# PowerShell
$env:KEYCLOAK_CLIENT_ID = "load-test-client"
$env:KEYCLOAK_CLIENT_SECRET = "your-secret"
k6 run load-tests/k6/scripts/agent-api.js

# Bash/Linux
export KEYCLOAK_CLIENT_ID="load-test-client"
export KEYCLOAK_CLIENT_SECRET="your-secret"
k6 run load-tests/k6/scripts/agent-api.js
```

#### Save Results

```powershell
k6 run `
  --out json=results/test-results.json `
  --summary-export=results/summary.json `
  load-tests/k6/scripts/agent-api.js
```

### Docker Execution (Optional)

```powershell
docker run --rm -i `
  -e KEYCLOAK_CLIENT_ID="load-test-client" `
  -e KEYCLOAK_CLIENT_SECRET="your-secret" `
  -v ${PWD}/load-tests:/load-tests `
  grafana/k6:latest run /load-tests/k6/scripts/agent-api.js
```

---

## CI/CD Integration

### GitHub Actions Workflows

#### 1. Manual Trigger (`load-test-manual.yaml`)

**Location**: `.github/workflows/load-test-manual.yaml`

**Features**:
- Manual trigger via GitHub Actions UI
- Select service (agent/core/web/all)
- Choose environment (production/development)
- Pick scenario (smoke/load/stress/spike/soak/ci)

**Usage**:
1. Go to **Actions** tab in GitHub
2. Select **"Load Test (Manual)"**
3. Click **"Run workflow"**
4. Configure options and run

#### 2. Scheduled Tests (`load-test-scheduled.yaml`)

**Location**: `.github/workflows/load-test-scheduled.yaml`

**Schedule**: Every Sunday at 2:00 AM UTC (10:00 AM CST)

**What it does**:
- Runs smoke tests on all services
- Generates weekly health report
- Alerts on threshold failures

#### 3. Reusable Workflow (`load-test-reusable.yaml`)

**Location**: `.github/workflows/load-test-reusable.yaml`

**Purpose**: Shared logic for running k6 tests

**Features**:
- Automated threshold checking
- Result artifact upload (30-day retention)
- GitHub Summary generation with key metrics
- Fail workflow if thresholds not met

### Required GitHub Secrets

Configure in **Settings** → **Secrets and variables** → **Actions**:

```
LOAD_TEST_KEYCLOAK_CLIENT_ID     = "load-test-client"
LOAD_TEST_KEYCLOAK_CLIENT_SECRET = "your-client-secret"
```

See [KEYCLOAK_SETUP.md](./KEYCLOAK_SETUP.md) for detailed setup instructions.

---

## Metrics & Thresholds

### Built-in k6 Metrics

| Metric | Description |
|--------|-------------|
| `http_reqs` | Total HTTP requests |
| `http_req_duration` | Request latency |
| `http_req_failed` | Failed requests rate |
| `vus` | Virtual users |
| `vus_max` | Peak virtual users |
| `checks` | Validation checks passed |

### Custom Metrics

#### Agent Service
- `mcp_tool_call_duration`: MCP tool execution time
- `auth_success_rate`: OAuth authentication success rate
- `mcp_tool_errors`: Count of MCP tool failures

#### Core Service
- `document_upload_duration`: File upload performance
- `database_query_duration`: Database operation latency
- `api_success_rate`: Overall API success rate

#### Web Service
- `static_resource_duration`: Static asset load time
- `page_load_duration`: Full page load latency
- `resource_success_rate`: Asset delivery success rate

### Viewing Results

#### Terminal Output

k6 provides real-time metrics during test execution and a summary at the end:

```
✓ get-teams: status is 200
✓ get-teams: response has data
✓ get-teams: response time < 500ms

checks.........................: 100.00% ✓ 450  ✗ 0
http_req_duration..............: avg=234ms  p(95)=389ms  p(99)=512ms
http_req_failed................: 0.00%   ✓ 0    ✗ 450
http_reqs......................: 450     15/s
```

#### GitHub Actions Summary

Workflow runs generate a detailed summary with:
- Key metrics table
- Threshold pass/fail status
- Failed threshold details
- Links to artifacts

#### Exported JSON

For deeper analysis, export results to JSON:

```powershell
k6 run --out json=results.json script.js
```

Analyze with `jq`:

```powershell
# Get average response time
jq '.metrics.http_req_duration.values.avg' results.json

# List failed checks
jq '.[] | select(.type=="Point" and .metric=="checks" and .data.value==0)' results.json
```

---

## Troubleshooting

### Issue: "OAuth authentication failed"

**Symptoms**:
```
OAuth authentication failed: 401 {"error":"invalid_client"}
```

**Solutions**:
1. Verify `KEYCLOAK_CLIENT_ID` and `KEYCLOAK_CLIENT_SECRET` are correct
2. Check client authentication is enabled in Keycloak
3. Ensure client secret hasn't been regenerated
4. See [KEYCLOAK_SETUP.md](./KEYCLOAK_SETUP.md) for configuration steps

### Issue: "Threshold failed: http_req_duration"

**Symptoms**:
```
✗ http_req_duration: ['p(95)<500']
  ↳  0% — ✓ 0 / ✗ 1
```

**Solutions**:
1. **Network latency**: Test from a closer location
2. **Service overload**: Reduce VUs or ramp-up rate
3. **Realistic thresholds**: Adjust thresholds in `thresholds.json`
4. **Scale services**: Increase replica count in Kubernetes

### Issue: "Failed to load script module"

**Symptoms**:
```
ERRO[0000] failed to load file: open(...): The system cannot find the file specified
```

**Solutions**:
1. Ensure you're in the repository root directory
2. Use relative paths correctly:
   ```powershell
   # Correct
   k6 run load-tests/k6/scripts/agent-api.js
   
   # Incorrect
   cd load-tests/k6/scripts
   k6 run agent-api.js  # Won't find ../config/...
   ```

### Issue: "Rate limited by Keycloak"

**Symptoms**:
```
OAuth authentication failed: 429 Too Many Requests
```

**Solutions**:
1. Reduce VUs in scenario configuration
2. Implement token caching (advanced)
3. Contact Keycloak admin to increase rate limits

### Issue: "Web test fails on static resources"

**Symptoms**:
```
✗ static: /assets/index.js status is 200 or 404
```

**Solutions**:
1. This is expected for some assets (Vite builds differently each time)
2. Update asset paths in `web-frontend.js` to match actual build output
3. Use browser dev tools to inspect actual asset names

---

## Best Practices

### 1. Start Small

Always begin with a smoke test to validate configuration:

```powershell
k6 run --env SCENARIO=smoke script.js
```

### 2. Gradual Ramp-Up

Use ramping scenarios to avoid overwhelming services:

```json
{
  "executor": "ramping-vus",
  "stages": [
    { "duration": "2m", "target": 10 },  // Warm-up
    { "duration": "5m", "target": 10 },  // Sustain
    { "duration": "2m", "target": 0 }    // Cool-down
  ]
}
```

### 3. Realistic User Behavior

Add `sleep()` between requests to simulate think time:

```javascript
export default function() {
  // Request 1
  http.get(url1);
  sleep(1);  // User reading time
  
  // Request 2
  http.post(url2, data);
  sleep(2);
}
```

### 4. Monitor Service Health

Before running load tests:
- Check service health endpoints
- Verify database connections
- Ensure sufficient resources

### 5. Clean Up Test Data

Delete test resources created during load tests:

```javascript
// Create
const res = http.post('/api/v1/teams', payload);
const teamId = res.json('id');

// ... perform tests ...

// Clean up
http.del(`/api/v1/teams/${teamId}`);
```

### 6. Version Control Results

Commit threshold changes and track performance over time:

```powershell
git add load-tests/k6/config/thresholds.json
git commit -m "chore(load-tests): Update thresholds after optimization"
```

### 7. Document Findings

After each significant test run, document:
- Test parameters (scenario, VUs, duration)
- Results summary
- Any issues discovered
- Action items for optimization

---

## Additional Resources

- **k6 Official Docs**: https://k6.io/docs/
- **k6 Examples**: https://k6.io/docs/examples/
- **OAuth with k6**: https://k6.io/docs/examples/oauth-authentication/
- **Keycloak Setup**: [KEYCLOAK_SETUP.md](./KEYCLOAK_SETUP.md)
- **Project README**: [../README.md](../README.md)

---

## Contributing

When adding new test scripts or modifying configuration:

1. Test locally before committing
2. Update this README if adding new features
3. Follow existing code style and structure
4. Add comments for complex logic
5. Update thresholds based on actual service performance

---

**Maintained by**: no-tang-doc Team  
**Last Updated**: 2025-11-04  
**k6 Version**: 0.54.0
