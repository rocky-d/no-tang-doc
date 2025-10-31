# no-tang-doc-agent

MCP (Model Context Protocol) Server for the no-tang-doc knowledge base project.

## Overview

This is the agent component of no-tang-doc, providing MCP server functionality that exposes API tools for AI agents to interact with the no-tang-doc knowledge base.

## Prerequisites

- Python 3.13.7+
- [uv](https://docs.astral.sh/uv/) - Python package and project manager
- Docker (for containerized deployment)

## Development Setup

### Install Dependencies

```bash
# Install uv if not already installed
# See: https://docs.astral.sh/uv/getting-started/installation/

# Install project dependencies
uv sync
```

### Running Locally

```bash
# Run the MCP server using the defined script
uv run no-tang-doc-agent-mcp-server \
  --base-url http://localhost:8070 \
  --host 0.0.0.0 \
  --port 8002 \
  --issuer-url http://auth.local:8080/realms/ntdoc

# Or view help for all options
uv run no-tang-doc-agent-mcp-server --help
```

### Running Tests

```bash
# Run tests with coverage
uv run pytest tests/ \
  --cov=src/no_tang_doc_agent \
  --cov-report=xml \
  --cov-report=html \
  --cov-report=term-missing
```

### Code Quality

```bash
# Run linter
uv run ruff check src/ tests/

# Auto-fix linting issues
uv run ruff check --fix src/ tests/

# Format code
uv run ruff format src/ tests/
```

## Docker Deployment

### Build Docker Image

```bash
docker build -t no-tang-doc-agent:latest .
```

### Run Container

```bash
docker run -d \
  -p 8002:8002 \
  --name no-tang-doc-agent \
  no-tang-doc-agent:latest \
  --base-url https://api.ntdoc.site \
  --log-level INFO \
  --host 0.0.0.0 \
  --port 8002 \
  --issuer-url https://auth.ntdoc.site/realms/ntdoc \
  --required-scopes email \
  --required-scopes profile \
  --required-scopes mcp-user
```

### Using Docker Compose

For local development with all services:

```bash
# Start all services (agent + core + mysql + keycloak)
docker-compose up -d

# View logs
docker-compose logs -f agent

# Stop all services
docker-compose down
```

For testing agent service only:

```bash
docker-compose -f docker-compose.test.yml up -d
```

## Configuration

The MCP server accepts the following command-line options:

| Option | Default | Description |
|--------|---------|-------------|
| `--base-url` | `http://localhost:8070` | Base URL for no-tang-doc-core API |
| `--name` | `no-tang-doc-agent-mcp-server` | Name of the MCP server |
| `--debug` | `true` | Enable debug mode |
| `--log-level` | `INFO` | Logging level (DEBUG, INFO, WARNING, ERROR, CRITICAL) |
| `--host` | `localhost` | Host address to bind the server |
| `--port` | `8002` | Port number to bind the server |
| `--issuer-url` | `http://auth.local:8080/realms/ntdoc` | OAuth2/OIDC issuer URL |
| `--required-scopes` | `email`, `profile`, `mcp-user` | Required OAuth2 scopes (can be specified multiple times) |

### Environment Variables

You can also configure the service using environment variables. See `.env.example` for all available options.

```bash
# Copy example env file
cp .env.example .env

# Edit configuration
# vim .env
```

## Project Structure

```
no-tang-doc-agent/
├── src/
│   └── no_tang_doc_agent/
│       ├── mcp_server/          # MCP server implementation
│       │   ├── __init__.py
│       │   ├── __main__.py      # Entry point
│       │   └── mcp_server.py    # FastMCP server logic
│       ├── mcp_client/          # MCP client (if any)
│       └── datetime_excepthook.py
├── tests/                        # Test files
├── pyproject.toml               # Project configuration
├── uv.lock                      # Dependency lock file
├── Dockerfile                   # Docker image definition
├── docker-compose.yml           # Docker Compose for full stack
├── docker-compose.test.yml      # Docker Compose for testing
└── README.md                    # This file
```

## CI/CD

The project uses GitHub Actions for CI/CD:

- **Linting**: `ruff check` and `ruff format --check`
- **Testing**: `pytest` with coverage reports
- **Coverage Threshold**: 90% minimum

See `.github/workflows/no-tang-doc-agent-ci.yaml` for details.

## Contributing

1. Create a feature branch from `dev`: `feat/agent/NTDOC-XX-description`
2. Make your changes
3. Ensure tests pass and coverage meets requirements
4. Submit a pull request to `dev` branch

## License

[Add license information here]

## Related Projects

- [no-tang-doc-core](../no-tang-doc-core/) - Backend API service
- [no-tang-doc-web](../no-tang-doc-web/) - Frontend web application
