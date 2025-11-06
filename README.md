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
  - [no-tang-doc-agent](#no-tang-doc-agent-mcp-server)
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
| **Web** | <https://ntdoc.site> | Frontend User Interface |
| **Agent** | <https://agent.ntdoc.site> | MCP Server API |
| **Core** | <https://api.ntdoc.site> | Backend REST API |
| **Auth** | <https://auth.ntdoc.site> | Keycloak Authentication |

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
          ┌──────────────┼──────────────┐
          │              │              │
     ┌────▼────┐    ┌────▼────┐    ┌────▼────┐
     │  Auth   │    │  Agent  │    │  Core   │
     │ Service │    │ Service │    │ Service │
     └────┬────┘    └────┬────┘    └────┬────┘
          │              │              │
          │         ┌────▼────┐   ┌─────▼────┐
          │         │   MCP   │   │  MySQL   │
          │         │  Tools  │   │ Database │
          │         └─────────┘   └──────────┘
          │
    ┌─────▼───────────────────────────────────┐
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
├── pyproject.toml             # Python project configuration
├── uv.lock                    # Dependency lock file
├── Dockerfile                 # Container image definition
├── docker-compose.yml         # Local development setup
├── logging.yaml               # Logging configuration
├── fastagent.config.yaml      # Fast-agent client config
├── .env.example               # Environment variables template
└── README.md                  # Service documentation
```

#### Configuration

For detailed configuration instructions, environment variables, and setup guides, please refer to [`no-tang-doc-agent/README.md`](no-tang-doc-agent/README.md).

#### Development Workflow

For detailed development setup, testing procedures, linting, and Docker workflows, please refer to [`no-tang-doc-agent/README.md`](no-tang-doc-agent/README.md).

**Quick Start**

```bash
cd no-tang-doc-agent
uv sync --all-extras --dev
uv run no-tang-doc-agent-mcp-server
```

#### Kubernetes Deployment

For detailed Helm chart configuration and deployment instructions, please refer to [`no-tang-doc-agent/README.md`](no-tang-doc-agent/README.md).

**Current Status**

- **Namespace**: `ntdoc-agent`
- **Replicas**: 1/1 Ready
- **Image**: `registry.digitalocean.com/ntdoc/ntdoc-agent`
- **Ingress Host**: `agent.ntdoc.site`
- **Ports**: 8002 (internal), 80/443 (ingress)

#### CI/CD Workflows

For detailed CI/CD workflow configurations and deployment procedures, please refer to [`no-tang-doc-agent/README.md`](no-tang-doc-agent/README.md).

**Agent CI (`.github/workflows/no-tang-doc-agent-ci.yaml`)**

Triggers: Push, PR, manual (`workflow_dispatch`), reusable (`workflow_call`)

Jobs: Lint → Test → Deploy (calls CD workflow)

**Agent CD (`.github/workflows/no-tang-doc-agent-cd.yaml`)**

Triggers: CI workflow call, manual trigger

Jobs: Build → Deploy

#### Container Image

For detailed Dockerfile configuration and build instructions, please refer to [`no-tang-doc-agent/README.md`](no-tang-doc-agent/README.md).

**Registry**: DigitalOcean Container Registry (DOCR)

- **Repository**: `registry.digitalocean.com/ntdoc/ntdoc-agent`

> **Note**: For comprehensive Agent service documentation including development setup, testing, deployment, and CI/CD workflows, please refer to [`no-tang-doc-agent/README.md`](no-tang-doc-agent/README.md).

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
- **Image**: `registry.digitalocean.com/ntdoc/ntdoc-core`
- **Replicas**: 1/1 Ready
- **Ingress Host**: `api.ntdoc.site`

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

- **Repository**: `registry.digitalocean.com/ntdoc/ntdoc-web`

#### Kubernetes Deployment

> **Note**: Web service deployment to Kubernetes is planned. Currently, the service is containerized and images are available in DOCR. For local development, see [`no-tang-doc-web/README.md`](no-tang-doc-web/README.md).

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
Load Balancer
    ├── ntdoc.site         → Web Frontend (80/443)
    ├── auth.ntdoc.site    → Keycloak (80/443)
    ├── api.ntdoc.site     → Core Service (80/443)
    └── agent.ntdoc.site   → Agent Service (80/443)
```

### Container Registry

**DigitalOcean Container Registry (DOCR)**

Registry: `registry.digitalocean.com/ntdoc`

**Available Repositories:**

- `ntdoc-agent` - Agent service images
- `ntdoc-core` - Core service images
- `ntdoc-web` - Web service images

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

For comprehensive development guide including setup, testing, linting, and Docker workflows, please refer to [`no-tang-doc-agent/README.md`](no-tang-doc-agent/README.md).

**Quick Start**

```bash
cd no-tang-doc-agent
uv sync --all-extras --dev
uv run no-tang-doc-agent-mcp-server
```

### Branch Strategy

**Protected Branches**

- `main`: Production releases
- `dev`: Development integration
- `docs`: Documentation updates (merges to `dev`)
- `mod/*`: Module-specific main branches
  - `mod/agent`: Agent service
  - `mod/core`: Core service
  - `mod/web`: Web service

**Feature Branches**

- `feat/{module}/*`: Feature development
  - Example: `feat/agent/<feature-name>`
- `feat/*`: Project-wide features
  - Example: `feat/<feature-name>`

**Workflow**

```
feat/{module}/* → mod/{module} → dev → main
      ↓ PR           ↓ PR        ↓ PR
  Feature Dev    Module Merge  Dev Test

feat/* (docs, etc.) → dev → main
      ↓ PR            ↓ PR
  Project Features  Dev Test
```

**Rules**

- Protected branches (`main`, `dev`, `docs`, `mod/*`) require Pull Requests
- No direct pushes to protected branches
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

For detailed Terraform configuration and usage, please refer to [`IaC/README.md`](IaC/README.md).

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

### Manual Deployment

For detailed deployment instructions and Helm configuration, please refer to service-specific README files:

- Agent: [`no-tang-doc-agent/README.md`](no-tang-doc-agent/README.md)
- Core: [`no-tang-doc-core/README.md`](no-tang-doc-core/README.md)
- Web: [`no-tang-doc-web/README.md`](no-tang-doc-web/README.md)

**Quick Example (Agent Service)**

```bash
doctl kubernetes cluster kubeconfig save ntdoc-doks
helm upgrade --install ntdoc-agent charts/ntdoc-agent \
  -n ntdoc-agent --create-namespace
kubectl -n ntdoc-agent get all,ingress
```

### Automated Deployment

Deployments are automatically triggered by:

1. Merging PRs to protected branches (`main`, `dev`, `mod/agent`)
2. CI workflow success
3. Manual workflow dispatch in GitHub Actions

---

## Contributing

We welcome contributions! Please see [`CONTRIBUTING.md`](CONTRIBUTING.md) for detailed guidelines on:

- Code guidelines and best practices
- Branch strategy and workflow
- Commit message conventions
- Pull request process
- Testing requirements
- Development environment setup

**Quick Start for Contributors:**

1. Fork the repository
2. Create a feature branch (`feat/{module}/your-feature`)
3. Make your changes with tests
4. Follow commit message conventions
5. Submit a pull request

For detailed instructions, please read [`CONTRIBUTING.md`](CONTRIBUTING.md).

---

## Monitoring & Observability

### Metrics

**Prometheus Integration**

- Core service exposes metrics via Spring Actuator
- Metrics endpoint: `/actuator/prometheus`
- Grafana dashboards (planned)

### Logging

For detailed logging configuration, please refer to service-specific README files.

**Agent Service**

- Structured logging with YAML configuration
- Kubernetes logs: `kubectl -n ntdoc-agent logs -f deployment/ntdoc-agent-ntdoc-agent`

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

- **MCP Protocol**: <https://modelcontextprotocol.io>
- **MCP Python SDK**: <https://github.com/modelcontextprotocol/python-sdk>
- **uv Package Manager**: <https://docs.astral.sh/uv/>
- **Spring Boot**: <https://spring.io/projects/spring-boot>
- **Kubernetes**: <https://kubernetes.io/docs/>
- **Helm**: <https://helm.sh/docs/>

### Internal Documentation

- **Infrastructure Guide**: `/IaC/README.md`
- **Agent Service**: `/no-tang-doc-agent/README.md`
- **Core Service**: `/no-tang-doc-core/README.md`
- **Web Service**: `/no-tang-doc-web/README.md`

### Support

For questions or issues:

1. Check existing documentation
2. Search GitHub Issues
3. Contact team leads
4. Create new issue with details

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

Copyright (c) 2025 Rocky Haotian Du

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
