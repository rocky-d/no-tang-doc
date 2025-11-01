# no-tang-doc

<div align="center">

**A Notion-like Document Knowledge Base System**

[![GitHub](https://img.shields.io/badge/GitHub-no--tang--doc-blue.svg)](https://github.com/rocky-d/no-tang-doc)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-DOKS-326CE5.svg)](https://www.digitalocean.com/products/kubernetes)

</div>

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Services](#services)
  - [no-tang-doc-agent](#no-tang-doc-agent)
  - [no-tang-doc-core](#no-tang-doc-core-backend-api)
  - [no-tang-doc-web](#no-tang-doc-web-frontend)
- [Infrastructure](#infrastructure)
  - [Kubernetes Deployment](#kubernetes-deployment)
  - [Container Registry](#container-registry)
  - [Authentication Service](#authentication-service)
- [Development Guide](#development-guide)
  - [Agent Service Development](#agent-service-development)
  - [Branch Strategy](#branch-strategy)
  - [CI/CD Workflows](#cicd-workflows)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

**no-tang-doc** is a modern, microservices-based document knowledge base system designed to provide a collaborative environment for document management and knowledge sharing. The system consists of three core services:

- **Agent**: MCP (Model Context Protocol) server exposing LLM-friendly APIs
- **Core**: Backend REST API service with Spring Boot
- **Web**: Frontend user interface built with React

### Key Features

- 📝 **Document Management**: Create, edit, share, and organize documents
- 👥 **Team Collaboration**: Team creation, member management, and permissions
- 🤖 **LLM Integration**: MCP server for AI assistant interactions
- 🔐 **OAuth 2.0 Authentication**: Unified authentication via Keycloak
- ☸️ **Cloud Native**: Kubernetes deployment on DigitalOcean
- 🚀 **CI/CD Automation**: GitHub Actions for continuous delivery

### Live Services

| Service | URL | Description |
|---------|-----|-------------|
| **Agent** | https://agent.ntdoc.site | MCP Server API |
| **Core** | https://api.ntdoc.site | Backend REST API |
| **Auth** | https://auth.ntdoc.site | Keycloak Authentication |

---

## Architecture

```
┌──────────────────────────────────────────────────┐
│                     Internet                     │
└────────────────────────┬─────────────────────────┘
                         │
                ┌────────▼────────┐
                │      Load       │
                │     Balancer    │
                └────────┬────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
    ┌────▼────┐    ┌────▼────┐    ┌────▼────┐
    │  Auth   │    │  Agent  │    │  Core   │
    │ Service │    │ Service │    │ Service │
    └────┬────┘    └────┬────┘    └────┬────┘
         │              │              │
         │         ┌────▼────┐    ┌────▼─────┐
         │         │   MCP   │    │  MySQL   │
         │         │  Tools  │    │ Database │
         │         └─────────┘    └──────────┘
         │
    ┌────▼────────────────────────────────────┐
    │            Keycloak Identity            │
    │       OAuth 2.0 / OpenID Connect        │
    └─────────────────────────────────────────┘
```

### Technology Stack

| Component | Technologies |
|-----------|-------------|
| **Agent** | Python 3.13, FastMCP, uv, OAuth 2.0 |
| **Core** | Java 24, Spring Boot 3.5, MySQL |
| **Web** | TypeScript, React, Vite, Radix UI |
| **Infrastructure** | Kubernetes (DOKS), Terraform, Helm |
| **CI/CD** | GitHub Actions, Docker, DOCR |
| **Monitoring** | Prometheus, Actuator |

---

## Services

### no-tang-doc-agent (MCP Server)

**MCP Server for LLM Integration**

The Agent service implements the Model Context Protocol (MCP), enabling Large Language Models to interact with the no-tang-doc system through a standardized interface.

#### Technology Stack

- **Language**: Python 3.13.7
- **Package Manager**: uv (Rust-based, 10-100x faster than pip)
- **Core Framework**: 
  - `fast-agent-mcp` ≥ 0.3.18
  - `mcp[cli]` ≥ 1.19.0
  - `pyjwt` ≥ 2.10.1
  - `pyyaml` ≥ 6.0.3

#### MCP Tools

The agent exposes 20 tools for LLM interactions:

**Document Management**
- `upload-document`: Upload new documents
- `download-document-content`: Retrieve document content
- `download-document-metadata`: Get document metadata
- `delete-document`: Remove documents
- `share-document`: Generate shareable links
- `get-documents`: List user's documents

**Team Management**
- `create-team`: Create new teams
- `get-team-by-id`: Retrieve team details
- `get-teams`: List user's teams
- `update-team-by-id`: Update team information
- `delete-team-by-id`: Remove teams
- `leave-team`: Leave a team

**Member Management**
- `add-team-member`: Add members to teams
- `remove-team-member`: Remove team members
- `update-team-member-role`: Change member roles
- `get-team-members`: List team members

**Analytics**
- `get-logs-list`: View operation logs
- `get-logs-count`: Get log statistics
- `get-logs-documents`: Retrieve document logs

**User**
- `get-api-auth-me`: Get current user info

#### Directory Structure

```
no-tang-doc-agent/
├── src/
│   └── no_tang_doc_agent/
│       └── mcp_server/
│           ├── __init__.py
│           └── __main__.py
├── tests/
│   └── test_mcp_server.py
├── archive/
│   ├── CLAUDE.md              # Development guidelines
│   ├── MCP-README.md          # MCP SDK documentation
│   └── uv速查表.md            # uv command reference
├── pyproject.toml             # Python project configuration
├── uv.lock                    # Dependency lock file
├── Dockerfile                 # Container image definition
├── docker-compose.yml         # Local development setup
├── logging.yaml               # Logging configuration
├── fastagent.config.yaml      # Fast-agent client config
├── .env.example               # Environment variables template
└── README.md                  # Service documentation
```

#### Configuration Files

**pyproject.toml**
```toml
[project]
name = "no-tang-doc-agent"
version = "0.1.0"
requires-python = ">=3.13.5,<3.14"
dependencies = [
    "fast-agent-mcp>=0.3.18",
    "mcp[cli]>=1.19.0",
    "pyjwt>=2.10.1",
    "pyyaml>=6.0.3",
]

[project.scripts]
no-tang-doc-agent-mcp-server = "no_tang_doc_agent.mcp_server.__main__:main"
```

**logging.yaml**
```yaml
version: 1
disable_existing_loggers: false

formatters:
  default:
    format: '%(asctime)s | %(name)s | %(levelname)s | %(message)s'
    datefmt: '%Y-%m-%d %H:%M:%S'

handlers:
  console:
    class: logging.StreamHandler
    level: INFO
    formatter: default
    stream: ext://sys.stdout

root:
  level: INFO
  handlers: [console]
```

**Environment Variables**
```bash
# Required
BASE_URL=https://api.ntdoc.site
ISSUER_URL=https://auth.ntdoc.site/realms/ntdoc
RESOURCE_SERVER_URL=https://agent.ntdoc.site/mcp
KEYCLOAK_CLIENT_SECRET=<secret>

# Optional
NAME=no-tang-doc-agent-mcp-server
DEBUG=true
LOG_LEVEL=INFO
HOST=0.0.0.0
PORT=8002
REQUIRED_SCOPES=["email", "profile", "mcp-user"]
```

#### Development Workflow

**Prerequisites**
- Python 3.13.7+
- uv package manager
- Docker (optional, for local containerization)

**Setup**
```bash
# Navigate to agent directory
cd no-tang-doc-agent

# Sync dependencies (creates .venv automatically)
uv sync --all-extras --dev

# Run the MCP server
uv run no-tang-doc-agent-mcp-server
```

**Testing**
```bash
# Run tests with coverage (95% required)
uv run pytest tests/ \
  --cov=src/no_tang_doc_agent/mcp_server \
  --cov-report=xml \
  --cov-report=term-missing \
  --cov-report=html \
  --cov-fail-under=95 \
  --cov-branch

# View HTML coverage report
open coverage_html/index.html
```

**Linting & Formatting**
```bash
# Run ruff check
uv run ruff check src/ tests/ --output-format=github

# Run ruff format check
uv run ruff format --check src/ tests/

# Auto-fix issues
uv run ruff check --fix src/ tests/
uv run ruff format src/ tests/
```

**Local Development with Docker**
```bash
# Build and run with docker-compose
docker-compose up --build

# Run in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

#### Kubernetes Deployment

**Current Status**
- **Namespace**: `ntdoc-agent`
- **Replicas**: 1/1 Ready
- **Image**: `registry.digitalocean.com/ntdoc/ntdoc-agent:dev-ba4702b`
- **Ingress Host**: `agent.ntdoc.site`
- **External IP**: `139.59.221.243`
- **Ports**: 8002 (internal), 80/443 (ingress)

**Helm Chart Structure**
```
charts/ntdoc-agent/
├── Chart.yaml
├── values.yaml
└── templates/
    ├── deployment.yaml
    ├── service.yaml
    ├── ingress.yaml
    ├── configmap.yaml
    └── secret.yaml
```

**Deployment Command**
```bash
helm upgrade --install ntdoc-agent charts/ntdoc-agent \
  -n ntdoc-agent --create-namespace \
  --set image.repository=registry.digitalocean.com/ntdoc/ntdoc-agent \
  --set image.tag=dev-ba4702b \
  --set ingress.hosts[0].host=agent.ntdoc.site \
  --set-string secrets.KEYCLOAK_CLIENT_SECRET='<secret>'
```

#### CI/CD Workflows

**Agent CI (`.github/workflows/no-tang-doc-agent-ci.yaml`)**

Triggers:
- Push to: `main`, `dev`, `mod/agent`, `feat/agent/**`
- Pull requests to: `main`, `dev`, `mod/agent`
- Manual trigger: `workflow_dispatch`
- Reusable: `workflow_call` (with `ref` parameter)

Jobs:
1. **Lint** (~8s): Ruff code quality checks
2. **Test** (~15s): Pytest with 95% coverage requirement
3. **Deploy**: Calls CD workflow (only on push to protected branches)

Features:
- Skips CI for mod/agent ↔ dev sync PRs
- Codecov integration
- Auto-comments test results on PRs
- Uploads test artifacts

**Agent CD (`.github/workflows/no-tang-doc-agent-cd.yaml`)**

Triggers:
- Called by CI workflow: `workflow_call`
- Manual trigger: `workflow_dispatch` (with branch selection and optional SHA)

Jobs:
1. **Build** (~37s): Docker image build and push to DOCR
2. **Deploy** (~29s): Helm deployment to DOKS

Features:
- Automatic SHA resolution (fetches latest commit if not specified)
- Image tagging strategy:
  - `main` branch → `sha-<short>`, `latest`
  - Other branches → `dev-<short>`, `dev`
- BuildKit cache optimization (GHA cache)
- GitHub Step Summary with deployment info

**Workflow Architecture**
```
┌─────────────────────────────────────────┐
│        GitHub Events                    │
│  (push, PR, manual, workflow_call)      │
└──────────────┬──────────────────────────┘
               │
       ┌───────▼────────┐
       │   Agent CI     │  ← Reusable component
       │  (3 jobs)      │     (workflow_call)
       └───────┬────────┘
               │
        ┌──────▼──────┐
        │   Lint      │  8s
        └──────┬──────┘
               │
        ┌──────▼──────┐
        │   Test      │  15s
        └──────┬──────┘
               │
        ┌──────▼──────┐
        │   Deploy    │  ← Calls CD workflow
        └──────┬──────┘
               │
       ┌───────▼────────┐
       │   Agent CD     │  ← Reusable component
       │  (2 jobs)      │     (workflow_call)
       └───────┬────────┘
               │
        ┌──────▼──────┐
        │   Build     │  37s
        └──────┬──────┘
               │
        ┌──────▼──────┐
        │   Deploy    │  29s
        └─────────────┘
```

#### Container Image

**Registry**: DigitalOcean Container Registry (DOCR)
- **Repository**: `registry.digitalocean.com/ntdoc/ntdoc-agent`
- **Latest Tag**: `dev-ba4702b`
- **Total Tags**: 16 versions
- **Last Updated**: 2025-11-01 12:38:10 UTC

**Dockerfile Highlights**
```dockerfile
FROM python:3.13-slim

WORKDIR /app

# Install uv
COPY --from=ghcr.io/astral-sh/uv:latest /uv /usr/local/bin/uv

# Copy project files
COPY pyproject.toml uv.lock ./
COPY logging.yaml ./
COPY src ./src

# Install dependencies
RUN uv sync --frozen --no-dev --no-cache

# Run MCP server
CMD ["uv", "run", "no-tang-doc-agent-mcp-server"]
```

---

### no-tang-doc-core (Backend API)

**RESTful API Service**

The Core service provides the main backend REST API for the no-tang-doc system.

#### Technology Stack

- **Language**: Java 24
- **Framework**: Spring Boot 3.5.5
- **Build Tool**: Maven
- **Database**: MySQL
- **Authentication**: Spring Security + OAuth2 Resource Server
- **Monitoring**: Spring Actuator + Micrometer (Prometheus)
- **AI Features**: Spring AI 1.0.1

#### Kubernetes Deployment

- **Namespace**: `ntdoc-core`
- **Image**: `registry.digitalocean.com/ntdoc/ntdoc-core:dev`
- **Replicas**: 1/1 Ready
- **Ingress Host**: `api.ntdoc.site`
- **External IP**: `139.59.221.243`

#### Directory Structure

```
no-tang-doc-core/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── config/
│   └── pmd/
├── docker/
│   └── mysql/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

> **Note**: Core service is maintained by a separate team. For detailed documentation, please refer to the Core team's documentation.

---

### no-tang-doc-web (Frontend)

**React-based User Interface**

The Web service provides the frontend user interface for the no-tang-doc system.

#### Technology Stack

- **Language**: TypeScript
- **Framework**: React
- **Build Tool**: Vite
- **UI Library**: Radix UI (25+ components)
- **Styling**: CSS Modules
- **Code Quality**: ESLint

#### Container Image

- **Repository**: `registry.digitalocean.com/ntdoc/ntdoc-web:dev`
- **Total Tags**: 3 versions
- **Last Updated**: 2025-11-01 04:20:40 UTC

#### Directory Structure

```
no-tang-doc-web/
├── src/
│   ├── components/
│   ├── pages/
│   ├── routes/
│   ├── styles/
│   ├── utils/
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css
├── public/
│   └── silent-check-sso.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── eslint.config.js
├── Dockerfile
└── docker-compose.yml
```

> **Note**: Web service is maintained by a separate team. For detailed documentation, please refer to the Web team's documentation.

---

## Infrastructure

### Kubernetes Deployment

**Cluster Information**
- **Provider**: DigitalOcean Kubernetes (DOKS)
- **Cluster Name**: `ntdoc-doks`
- **Region**: Singapore (sgp1)
- **Version**: 1.33.1-do.5
- **Node Pool**: `ntdoc-pool`

**Namespaces**
```
├── cert-manager      # TLS certificate management
├── default           # Default namespace
├── dev               # Development environment
├── external-dns      # DNS management
├── ingress-nginx     # Ingress controller
├── keycloak          # Authentication service
├── kube-node-lease   # Node heartbeat
├── kube-public       # Public resources
├── kube-system       # System components
├── ntdoc-agent       # Agent service
└── ntdoc-core        # Core service
```

**Ingress Routes**
```
139.59.221.243 (Load Balancer)
    ├── auth.ntdoc.site    → Keycloak (80/443)
    ├── api.ntdoc.site     → Core Service (80/443)
    └── agent.ntdoc.site   → Agent Service (80/443)
```

### Container Registry

**DigitalOcean Container Registry (DOCR)**

Registry: `registry.digitalocean.com/ntdoc`

| Repository | Latest Tag | Total Tags | Last Updated |
|------------|-----------|------------|--------------|
| `ntdoc-agent` | dev | 16 | 2025-11-01 12:38 UTC |
| `ntdoc-core` | dev | 3 | 2025-11-01 04:11 UTC |
| `ntdoc-web` | dev | 3 | 2025-11-01 04:20 UTC |

### Authentication Service

**Keycloak OAuth 2.0 / OpenID Connect**

- **Deployment Type**: StatefulSet (persistent)
- **Namespace**: `keycloak`
- **Replicas**: 1/1 Ready
- **Ingress Host**: `auth.ntdoc.site`
- **Realm**: `ntdoc`
- **Required Scopes**: `email`, `profile`, `mcp-user`

**Features**
- Centralized identity management
- OAuth 2.0 / OIDC authentication
- SSO (Single Sign-On)
- User and role management

---

## Development Guide

### Agent Service Development

Refer to the [no-tang-doc-agent](#no-tang-doc-agent) section above for detailed development instructions.

**Quick Start**
```bash
cd no-tang-doc-agent
uv sync --all-extras --dev
uv run no-tang-doc-agent-mcp-server
```

**Key Commands**
```bash
# Testing
uv run pytest tests/ --cov --cov-fail-under=95

# Linting
uv run ruff check src/ tests/
uv run ruff format src/ tests/

# Docker
docker-compose up --build
```

### Branch Strategy

**Protected Branches**
- `main`: Production releases
- `dev`: Development integration
- `mod/*`: Module-specific main branches
  - `mod/agent`: Agent service
  - `mod/core`: Core service
  - `mod/web`: Web service

**Feature Branches**
- `feat/{module}/*`: Feature development
  - Example: `feat/agent/NTDOC-77-MCP-Server`
- `feat/*`: Project-wide features
  - Example: `feat/project-documentation`

**Workflow**
```
feat/{module}/* → mod/{module} → dev → main
      ↓ PR           ↓ PR        ↓ PR
  Feature Dev    Module Merge  Dev Test
```

**Rules**
- Protected branches require Pull Requests
- No direct pushes to `main`, `dev`, or `mod/*`
- CI must pass before merging
- At least one approval required

### CI/CD Workflows

**Available Workflows**

| Workflow | File | Triggers | Purpose |
|----------|------|----------|---------|
| Agent CI | `no-tang-doc-agent-ci.yaml` | push, PR, manual, reusable | Code quality & orchestration |
| Agent CD | `no-tang-doc-agent-cd.yaml` | CI call, manual | Build & deploy |
| Core CI | `no-tang-doc-core-ci.yaml` | push, PR | Java build & test |
| Deploy to DOKS | `deploy-to-doks.yaml` | manual | Generic deployment |

**CI/CD Features**
- ✅ Automatic testing with coverage requirements
- ✅ Docker image building with BuildKit cache
- ✅ Helm-based Kubernetes deployment
- ✅ GitHub Actions workflow status badges
- ✅ Artifact uploads (test results, coverage reports)
- ✅ PR auto-comments with test results
- ✅ Manual workflow triggers with parameters

---

## Deployment

### Prerequisites

**Required Tools**
- `kubectl`: Kubernetes CLI
- `helm`: Kubernetes package manager
- `doctl`: DigitalOcean CLI
- `docker`: Container runtime
- `terraform`: Infrastructure as Code (optional)

**Required Secrets**
- `DO_ACCESS_TOKEN`: DigitalOcean API token
- `DOKS_CLUSTER_NAME`: Kubernetes cluster name
- `KEYCLOAK_CLIENT_SECRET`: OAuth client secret

### Infrastructure as Code

**Terraform Modules**
```
IaC/
├── cluster/              # DOKS cluster
├── cluster-bootstrap/    # Cluster initialization
├── database/            # Database resources
├── docr/                # Container registry
├── keycloak/            # Keycloak deployment
├── space/               # Object storage
└── addons/
    ├── cert-manager/    # TLS certificates
    ├── external-dns/    # DNS management
    └── ingress-nginx/   # Ingress controller
```

**Terraform Workflow**
```bash
# Set DO credentials
export AWS_ACCESS_KEY_ID="<spaces_key_id>"
export AWS_SECRET_ACCESS_KEY="<spaces_secret>"

# Initialize
terraform init

# Plan changes
terraform plan

# Apply changes
terraform apply
```

### Manual Deployment

**Agent Service**
```bash
# Connect to cluster
doctl kubernetes cluster kubeconfig save ntdoc-doks

# Deploy with Helm
helm upgrade --install ntdoc-agent charts/ntdoc-agent \
  -n ntdoc-agent --create-namespace \
  --set image.repository=registry.digitalocean.com/ntdoc/ntdoc-agent \
  --set image.tag=dev \
  --set ingress.hosts[0].host=agent.ntdoc.site \
  --set-string secrets.KEYCLOAK_CLIENT_SECRET='<secret>'

# Verify deployment
kubectl -n ntdoc-agent get all,ingress
```

### Automated Deployment

Deployments are automatically triggered by:
1. Merging PRs to protected branches (`main`, `dev`, `mod/agent`)
2. CI workflow success
3. Manual workflow dispatch in GitHub Actions

---

## Contributing

### Code Guidelines

**General Principles**
- Follow industry best practices
- High cohesion, low coupling
- Write clean, self-documenting code
- Include comprehensive tests (95% coverage for Agent)

**Agent Service Specific**
- Python 3.13.7+
- Use `uv` for all package management (no `pip` or `python` directly)
- Follow Ruff linting rules
- All code and comments in English
- Use type hints for all functions

**Commit Messages**
```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

Types: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `ci`

Example:
```
feat(agent): Add workflow_dispatch support to CI workflow

- Add manual trigger capability
- Support ref parameter for custom checkout
- Enable workflow reusability
```

### Pull Request Process

1. Create feature branch from appropriate base
2. Implement changes with tests
3. Ensure CI passes (lint, test, coverage)
4. Create PR with descriptive title and body
5. Request review from team members
6. Address review comments
7. Merge after approval

### Testing Requirements

**Agent Service**
- Unit test coverage: ≥95%
- All new features must have tests
- Run tests locally before pushing
- Check coverage report

```bash
cd no-tang-doc-agent
uv run pytest tests/ --cov --cov-fail-under=95
```

---

## Monitoring & Observability

### Metrics

**Prometheus Integration**
- Core service exposes metrics via Spring Actuator
- Metrics endpoint: `/actuator/prometheus`
- Grafana dashboards (planned)

### Logging

**Agent Service**
- Structured logging with YAML configuration
- Log levels: DEBUG, INFO, WARNING, ERROR, CRITICAL
- Kubernetes logs accessible via `kubectl logs`

```bash
# View agent logs
kubectl -n ntdoc-agent logs -f deployment/ntdoc-agent-ntdoc-agent

# View logs from specific time
kubectl -n ntdoc-agent logs --since=1h deployment/ntdoc-agent-ntdoc-agent
```

### Health Checks

**Kubernetes Probes**
- Liveness probes configured for all services
- Readiness probes ensure traffic only to healthy pods

**Service Health**
```bash
# Check all pods
kubectl get pods --all-namespaces

# Check specific service
kubectl -n ntdoc-agent get pods
kubectl -n ntdoc-core get pods
```

---

## Troubleshooting

### Common Issues

**Agent Service Won't Start**
```bash
# Check pod logs
kubectl -n ntdoc-agent logs deployment/ntdoc-agent-ntdoc-agent

# Check events
kubectl -n ntdoc-agent get events --sort-by='.lastTimestamp'

# Describe pod for details
kubectl -n ntdoc-agent describe pod <pod-name>
```

**Authentication Errors**
```bash
# Verify Keycloak is running
kubectl -n keycloak get pods

# Check ingress
kubectl -n keycloak get ingress

# Test OAuth endpoint
curl https://auth.ntdoc.site/realms/ntdoc/.well-known/openid-configuration
```

**CI/CD Failures**
- Check GitHub Actions workflow logs
- Verify secrets are set in repository settings
- Ensure Docker registry credentials are valid
- Check Kubernetes cluster connectivity

---

## Resources

### Documentation

- **MCP Protocol**: https://modelcontextprotocol.io
- **MCP Python SDK**: https://github.com/modelcontextprotocol/python-sdk
- **uv Package Manager**: https://docs.astral.sh/uv/
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Kubernetes**: https://kubernetes.io/docs/
- **Helm**: https://helm.sh/docs/

### Internal Documentation

- **Agent Development**: `/no-tang-doc-agent/archive/CLAUDE.md`
- **MCP SDK Guide**: `/no-tang-doc-agent/archive/MCP-README.md`
- **uv Reference**: `/no-tang-doc-agent/archive/uv速查表.md`
- **Infrastructure Guide**: `/IaC/README.md`

### Support

For questions or issues:
1. Check existing documentation
2. Search GitHub Issues
3. Contact team leads
4. Create new issue with details

---

## License

[Specify your license here]

---

## Team

**Agent Team**
- Focus: MCP server, Python development, CI/CD
- Repository: `no-tang-doc-agent/`

**Core Team**
- Focus: Backend API, Java development, database
- Repository: `no-tang-doc-core/`

**Web Team**
- Focus: Frontend UI, TypeScript development
- Repository: `no-tang-doc-web/`

---

<div align="center">

**Built with ❤️ by the no-tang-doc team**

</div>
