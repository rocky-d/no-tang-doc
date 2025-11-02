# no-tang-doc-agent: AI Assistant Context Guide

This document provides essential context and guidelines for AI assistants working on the **no-tang-doc-agent** subproject.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Technology Stack](#technology-stack)
- [Development Environment](#development-environment)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [CI/CD](#cicd)
- [Critical Rules](#critical-rules)
- [Key Resources](#key-resources)

---

## Project Overview

**no-tang-doc** is a Notion-like document knowledge base system with three microservices:
- **no-tang-doc-agent** (This project) - MCP Server for AI assistant integration
- **no-tang-doc-core** - Spring Boot REST API backend
- **no-tang-doc-web** - React frontend

### Agent Responsibilities

The agent is an MCP (Model Context Protocol) server that:
- Exposes Core API as MCP tools for AI assistants
- Implements OAuth 2.0 authentication via Keycloak
- Provides access to documents, teams, and logging features

### Live Services

| Service | URL |
|---------|-----|
| Agent | https://agent.ntdoc.site |
| Core API | https://api.ntdoc.site |
| Keycloak | https://auth.ntdoc.site |
| Web UI | https://ntdoc.site |

### Repository

- **URL**: https://github.com/rocky-d/no-tang-doc
- **Default Branch**: `dev`
- **Protected Branches**: `main`, `dev`, `docs`, `mod/*` (require PR)
- **Development Branches**: `feat/{module}/*`

---

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Python | 3.13.7+ |
| Package Manager | [uv](https://docs.astral.sh/uv/) | Latest |
| MCP Framework | FastMCP | 0.3.18+ |
| HTTP Client | httpx | Latest |
| Linter/Formatter | [Ruff](https://docs.astral.sh/ruff/) | 0.14.2+ |
| Testing | [pytest](https://docs.pytest.org/) | 8.4.2+ |
| Auth | PyJWT | 2.10.1+ |

### MCP Tools Exposed

The server exposes 20+ MCP tools organized by feature:

**Teams**: get-teams, create-team, update-team, delete-team, get-team-members, add-team-member, remove-team-member, update-team-member-role, leave-team

**Documents**: upload-document, get-documents, download-document-metadata, download-document-content, share-document, delete-document

**Logs**: get-logs-list, get-logs-documents, get-logs-count

**User**: get-api-auth-me

---

## Development Environment

### Prerequisites

- Python 3.13.7+
- [uv](https://docs.astral.sh/uv/) - Package manager (learn from official docs)
- Docker Desktop
- Git

### Setup

```powershell
# Navigate to agent directory
cd c:\rocky_d\code\no-tang-doc\no-tang-doc-agent

# Install dependencies
uv sync --all-extras --dev

# Run server locally
uv run no-tang-doc-agent-mcp-server \
  --base-url http://localhost:8070 \
  --host 0.0.0.0 \
  --port 8002 \
  --issuer-url http://auth.local:8080/realms/ntdoc
```

### Environment Variables

Key variables (see `.env.example`):
- `BASE_URL` - Core API URL
- `ISSUER_URL` - Keycloak OAuth issuer
- `REQUIRED_SCOPES` - OAuth scopes (e.g., "email profile mcp-user")

### Local Environment Notes

- **OS**: Windows 10 Pro with PowerShell 7
- **No Linux commands**: Use PowerShell equivalents (e.g., `Select-String` instead of `grep`)
- **Always in agent directory**: Run uv commands from `/no-tang-doc-agent/`

---

## Development Workflow

### Branch Strategy

- **Protected**: `main`, `dev`, `docs`, `mod/*` (require PR)
- **Development**: `feat/{module}/*` (e.g., `feat/agent/<name>`)

**Typical workflow:**
```powershell
# Create feature branch from dev
git checkout dev
git pull origin dev
git checkout -b feat/agent/<name>

# Make changes, commit, push
git add .
git commit -m "feat(agent): Implement feature"
git push -u origin feat/agent/<name>

# Create PR to mod/agent (then mod/agent → dev → main)
gh pr create --base mod/agent --title "<title>"
```

### Running Locally

```powershell
# Development server
uv run no-tang-doc-agent-mcp-server \
  --base-url http://localhost:8070 \
  --log-level DEBUG

# Docker Compose (assumes core is running)
docker-compose up -d
```

---

## Coding Standards

### CRITICAL: English-Only Codebase

**All code, comments, docstrings, variable names MUST be in English.**
- No Chinese or non-English characters in code
- Exception: Communication with humans can be in Chinese
- This is a strict, non-negotiable team standard

### Python Style

- Follow PEP 8 with Ruff enforcement
- Line length: 88 characters
- Use type hints for all functions
- Async/await for all I/O operations

**Example:**
```python
async def fetch_team(
    ctx: Context[ServerSession, None],
    team_id: int,
) -> dict[str, Any]:
    """
    Fetch team data from Core API.
    
    Args:
        ctx: MCP context with auth headers
        team_id: Team identifier
        
    Returns:
        Team data dictionary
    """
    authorization = ctx.request_context.request.headers["authorization"]
    async with httpx.AsyncClient(
        headers={"Authorization": authorization}
    ) as client:
        response = await client.get(f"{base_url}/api/v1/teams/{team_id}")
        response.raise_for_status()
        return response.json()
```

### Code Quality Commands

```powershell
# Lint and fix
uv run ruff check --fix src/ tests/

# Format code
uv run ruff format src/ tests/
```

---

## Testing

### Requirements

- **Minimum coverage**: 95% (enforced in CI)
- **Framework**: pytest with async support
- All new features must include tests

### Running Tests

```powershell
# Run all tests with coverage
uv run pytest tests/ \
  --cov=src/no_tang_doc_agent/mcp_server \
  --cov-report=html \
  --cov-report=term-missing \
  --cov-fail-under=95

# Run specific test
uv run pytest tests/test_mcp_server.py::test_name -v

# View HTML coverage report
start coverage_html\index.html
```

### Test Example

```python
import pytest
from unittest.mock import Mock

class TestMCPServer:
    @pytest.mark.asyncio
    async def test_verify_token(self):
        """Test JWT token verification."""
        verifier = JWTTokenVerifier()
        token = "valid.jwt.token"
        result = await verifier.verify_token(token)
        
        assert result is not None
        assert result.client_id == "expected-id"
```

---

## CI/CD

### GitHub Actions

**CI Workflow** (`.github/workflows/no-tang-doc-agent-ci.yaml`):
- Triggers: Push/PR to `main`, `dev`, `mod/agent`, `feat/agent/**`
- Jobs: Lint (Ruff) + Test (pytest with 95% coverage)
- Auto-comments PR with coverage results

**CD Workflow** (`.github/workflows/no-tang-doc-agent-cd.yaml`):
- Builds Docker image → DigitalOcean Container Registry
- Deploys to DOKS via Helm
- Tags: `main` → `latest`, other → `dev`

### Pre-commit Checklist

Before pushing:
1. ✅ Fix lint: `uv run ruff check --fix src/ tests/`
2. ✅ Format: `uv run ruff format src/ tests/`
3. ✅ Test: `uv run pytest tests/ --cov`
4. ✅ Coverage ≥ 95%
5. ✅ English-only code

---

## Critical Rules

### Non-Negotiable Requirements

1. **English-Only**: All code, comments, variable names in English (no exceptions)
2. **Use uv**: Never use `python` or `pip` directly, always use `uv` commands
3. **Working Directory**: Always in `/no-tang-doc-agent/` when running uv
4. **Protected Branches**: Never push to `main`/`dev`/`docs`/`mod/*` - use PR only
5. **Test Coverage**: Minimum 95% coverage required
6. **Type Hints**: Required for all function parameters and returns
7. **Async First**: Use async/await for all I/O operations

### Development Constraints

- **OS**: Windows 10 Pro + PowerShell 7 (no Linux commands)
- **Python**: 3.13.7+ (strictly enforced)
- **OAuth**: All MCP tools require Keycloak authentication
- **MCP Spec**: Follow https://modelcontextprotocol.io/specification
- **Branch Flow**: feat/agent/* → mod/agent → dev → main

### Security

- Never commit secrets (use env vars or K8s secrets)
- Always verify JWT tokens before processing
- Forward auth headers from context to Core API
- Production must use HTTPS only

---

## Key Resources

### Official Documentation

**Project**:
- Repository: https://github.com/rocky-d/no-tang-doc
- Agent README: `/no-tang-doc-agent/README.md`
- IaC Guide: `/IaC/README.md`

**Core Technologies**:
- **uv**: https://docs.astral.sh/uv/ (Learn package management)
- **MCP Spec**: https://modelcontextprotocol.io/
- **MCP Python SDK**: https://github.com/modelcontextprotocol/python-sdk
- **Ruff**: https://docs.astral.sh/ruff/
- **pytest**: https://docs.pytest.org/
- **Python 3.13**: https://docs.python.org/3.13/

**Infrastructure**:
- Deployment details in `/charts/ntdoc-agent/`
- Terraform configs in `/IaC/`

### When You Need Help

1. Check project README files
2. Consult official docs for the relevant technology
3. Review existing code patterns for consistency
4. Search online for best practices
5. List questions clearly with recommendations

---

**Version**: 1.0.0 | **Last Updated**: 2025-11-01  
**For**: AI assistants working on no-tang-doc-agent

