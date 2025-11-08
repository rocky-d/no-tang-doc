# Multi-stage build for no-tang-doc-agent MCP Server
# Python version aligned with project requirements
ARG PYTHON_VERSION=3.13
# Build-time default port for documentation (can be overridden with --build-arg PORT=...)
ARG PORT=8002
ARG DEBUG=false

# Use a small helper stage to install uv so we don't rely on COPY from a remote image
FROM ghcr.io/astral-sh/uv:latest AS uv-stage

# ========== Runtime Stage ==========
FROM python:${PYTHON_VERSION}-slim-bookworm

WORKDIR /app

# Install uv for dependency management
COPY --from=uv-stage /uv /usr/local/bin/uv

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

# Expose default MCP server port (build-time, documentation only)
# Use build ARG so image can declare a different default port when built with --build-arg PORT=...
EXPOSE ${PORT}

# Configure Python to run in unbuffered mode for better logging
ENV PYTHONUNBUFFERED=1

# Allow passing build-time DEBUG and expose as runtime env var. Default is false.
ENV DEBUG=${DEBUG}

# Health check using socket connection and respect PORT env var
# Use uv to run python so uv is consistently used for Python-related tasks.
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD uv run python -c "import os,socket; p=int(os.getenv('PORT','8002')); s=socket.socket(); s.settimeout(5); s.connect(('127.0.0.1', p)); s.close()" || exit 1

# Use uv to run the application via the defined script
# Environment variables from Helm values.yaml will be used
ENTRYPOINT ["uv", "run", "--frozen", "--no-dev", "no-tang-doc-agent-mcp-server"]
