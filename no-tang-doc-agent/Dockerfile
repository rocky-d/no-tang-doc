# Multi-stage build for no-tang-doc-agent MCP Server
# Python version aligned with project requirements
ARG PYTHON_VERSION=3.13

# ========== Runtime Stage ==========
FROM python:${PYTHON_VERSION}-slim-bookworm

WORKDIR /app

# Install uv for dependency management
COPY --from=ghcr.io/astral-sh/uv:latest /uv /usr/local/bin/uv

# Copy project files
COPY pyproject.toml uv.lock README.md logging.yaml ./
COPY src ./src

# Create non-root user before installing dependencies
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup --home /home/appuser appuser && \
    mkdir -p /home/appuser/.cache && \
    chown -R appuser:appgroup /home/appuser

# Install dependencies using uv (as root), then change ownership
RUN --mount=type=cache,target=/root/.cache/uv \
    uv sync --frozen --no-dev && \
    chown -R appuser:appgroup /app

USER appuser

# Expose default MCP server port
EXPOSE 8002

# Configure Python to run in unbuffered mode for better logging
ENV PYTHONUNBUFFERED=1

# Health check using socket connection
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD uv run python -c "import socket; s=socket.socket(); s.settimeout(5); s.connect(('127.0.0.1', 8002)); s.close()" || exit 1

# Use uv to run the application via the defined script
# Environment variables from Helm values.yaml will be used
ENTRYPOINT ["uv", "run", "--frozen", "--no-dev", "no-tang-doc-agent-mcp-server"]
