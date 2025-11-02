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

For local development (assumes core backend is already running):

```bash
# Step 1: Start backend services (if not already running)
cd ../no-tang-doc-core
docker-compose up -d
cd ../no-tang-doc-agent

# Step 2: Copy and configure environment variables (optional)
cp .env.example .env
# Edit .env if needed (default values should work for local development)

# Step 3: Start agent service
docker-compose up --build -d

# View logs
docker-compose logs -f agent

# Stop service
docker-compose down
```

**Prerequisites:** 
- no-tang-doc-core and keycloak must be running on host machine (ports 8070 and 8080)
- The agent uses `host.docker.internal` to access services running on the host
- For Linux users, ensure Docker version supports `host.docker.internal` or manually configure host IP

## Using fast-agent as MCP Client

[fast-agent](https://fast-agent.ai/) is a powerful MCP client that enables you to interact with MCP servers through simple declarative syntax. It supports both stdio and HTTP transports with OAuth authentication.

### Installation

```bash
# Install fast-agent using uv
uv pip install fast-agent-mcp
```

### Quick Start

Start an interactive session with the MCP server:

```bash
# Using local server (requires server to be running on localhost:8002)
fast-agent go --url http://localhost:8002/mcp

# Using production server (requires OAuth authentication)
fast-agent go --url https://agent.ntdoc.site/mcp
```

### Configuration

The project includes a `fastagent.config.yaml` file that defines MCP server connections. For the no-tang-doc agent:

```yaml
mcp:
  servers:
    no-tang-doc-agent-mcp-server:
      transport: http
      url: https://agent.ntdoc.site/mcp  # or http://localhost:8002/mcp for local
      auth:
        oauth: true
        redirect_port: 3030
        redirect_path: /callback
```

### Creating Agents with fast-agent

You can create custom agents that use the no-tang-doc MCP server:

```python
import asyncio
from fast_agent import FastAgent

# Initialize FastAgent (reads fastagent.config.yaml)
fast = FastAgent("Knowledge Base Assistant")

@fast.agent(
    name="kb_agent",
    instruction="You are a helpful assistant that can manage documents in the no-tang-doc knowledge base.",
    servers=["no-tang-doc-agent-mcp-server"],  # Reference the server from config
)

async def main():
    async with fast.run() as agent:
        # Interactive chat session
        await agent.kb_agent()
        
        # Or send a direct message
        result = await agent.kb_agent("List all spaces")
        print(result)

if __name__ == "__main__":
    asyncio.run(main())
```

Save as `kb_agent.py` and run with:

```bash
uv run kb_agent.py
```

### OAuth Authentication

fast-agent automatically handles OAuth authentication for HTTP MCP servers:

1. **First Connection**: Opens browser for authentication
2. **Token Storage**: Securely stores tokens in OS keychain via `keyring`
3. **Auto-Refresh**: Automatically refreshes expired tokens

To force re-authentication:

```bash
# Clear stored tokens
fast-agent auth clear no-tang-doc-agent-mcp-server
```

### Advanced Usage

#### Using Multiple Models

```bash
# Specify a model for the agent
uv run kb_agent.py --model sonnet       # Claude Sonnet
uv run kb_agent.py --model gpt-4.1      # GPT-4 Turbo
uv run kb_agent.py --model o3-mini.low  # O3-mini with low reasoning
```

#### Agent Workflows

Create complex workflows combining multiple agents:

```python
@fast.agent(
    "searcher",
    "Search for documents in the knowledge base",
    servers=["no-tang-doc-agent-mcp-server"]
)

@fast.agent(
    "summarizer",
    "Create a concise summary of the provided content"
)

@fast.chain(
    name="search_and_summarize",
    sequence=["searcher", "summarizer"]
)

async def main():
    async with fast.run() as agent:
        result = await agent.search_and_summarize("Find documents about API design")
```

#### Command Line Usage

```bash
# Direct message to agent
uv run kb_agent.py --agent kb_agent --message "Create a new space called 'Projects'"

# Quiet mode (only shows final result)
uv run kb_agent.py --agent kb_agent --message "List documents" --quiet
```

For more information, see:
- [fast-agent Documentation](https://fast-agent.ai/)
- [fast-agent GitHub](https://github.com/evalstate/fast-agent-mcp)
- [MCP Specification](https://modelcontextprotocol.io/)

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
├── docker-compose.yml           # Docker Compose configuration
├── logging.yaml                 # Logging configuration
├── .env.example                 # Environment variables template
└── README.md                    # This file
```

## CI/CD

The project uses GitHub Actions for CI/CD:

- **Linting**: `ruff check` and `ruff format --check`
- **Testing**: `pytest` with coverage reports
- **Coverage Threshold**: 95% minimum

See `.github/workflows/no-tang-doc-agent-ci.yaml` for details.

## Contributing

Please refer to [CONTRIBUTING.md](../CONTRIBUTING.md) in the repository root for contribution guidelines.

## License

This project is licensed under the terms specified in [LICENSE](../LICENSE) in the repository root.

## Related Projects

- [no-tang-doc-core](../no-tang-doc-core/) - Backend API service
- [no-tang-doc-web](../no-tang-doc-web/) - Frontend web application
