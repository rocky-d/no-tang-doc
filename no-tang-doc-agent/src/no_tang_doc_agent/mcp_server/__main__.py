import logging

import click
from mcp.server.auth.settings import AuthSettings
from pydantic import AnyHttpUrl

from no_tang_doc_agent.datetime_excepthook import apply_datetime_excepthook
from no_tang_doc_agent.mcp_server import (
    FastMCPSettings,
    JWTTokenVerifier,
    start_mcp_server,
)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class ScopesParamType(click.ParamType):
    """Custom parameter type for parsing scopes from environment variable or CLI."""

    name = "scopes"

    def convert(self, value, param, ctx):
        """Convert space-separated string to list of scopes."""
        if isinstance(value, str):
            # Split by space for environment variable
            return value.split()
        return value


@click.command()
@click.option(
    "--base-url",
    envvar="BASE_URL",
    default="http://localhost:8070",
    help="Base URL for the MCP server",
    show_default=True,
)
@click.option(
    "--name",
    envvar="SERVER_NAME",
    default="no-tang-doc-agent-mcp-server",
    help="Name of the MCP server",
    show_default=True,
)
@click.option(
    "--debug",
    is_flag=True,
    default=True,
    help="Enable debug mode",
    show_default=True,
)
@click.option(
    "--log-level",
    envvar="LOG_LEVEL",
    default="INFO",
    type=click.Choice(
        ["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"], case_sensitive=False
    ),
    help="Logging level",
    show_default=True,
)
@click.option(
    "--host",
    envvar="HOST",
    default="localhost",
    help="Host address to bind the server",
    show_default=True,
)
@click.option(
    "--port",
    envvar="PORT",
    default=8002,
    type=int,
    help="Port number to bind the server",
    show_default=True,
)
@click.option(
    "--issuer-url",
    envvar="ISSUER_URL",
    default="http://auth.local:8080/realms/ntdoc",
    help="OAuth2/OIDC issuer URL",
    show_default=True,
)
@click.option(
    "--resource-server-url",
    envvar="RESOURCE_SERVER_URL",
    default="https://agent.ntdoc.site/mcp",
    help="Resource server URL",
    show_default=True,
)
@click.option(
    "--required-scopes",
    envvar="REQUIRED_SCOPES",
    type=ScopesParamType(),
    default="email profile mcp-user",
    help="Required OAuth2 scopes (space-separated string or can be specified multiple times).",
    show_default=True,
)
def main(
    base_url: str,
    name: str,
    debug: bool,
    log_level: str,
    host: str,
    port: int,
    issuer_url: str,
    resource_server_url: str,
    required_scopes: list[str],
) -> None:
    logger.info(f"{base_url=}")
    logger.info(f"{name=}")
    logger.info(f"{debug=}")
    logger.info(f"{log_level=}")
    logger.info(f"{host=}")
    logger.info(f"{port=}")
    logger.info(f"{issuer_url=}")
    logger.info(f"{resource_server_url=}")
    logger.info(f"{required_scopes=}")
    start_mcp_server(
        base_url=base_url,
        mcp_settings=FastMCPSettings(
            name=name,
            debug=debug,
            log_level=log_level,
            host=host,
            port=port,
            token_verifier=JWTTokenVerifier(),
            auth=AuthSettings(
                issuer_url=AnyHttpUrl(issuer_url),
                resource_server_url=AnyHttpUrl(resource_server_url),
                required_scopes=required_scopes,
            ),
        ),
    )


if __name__ == "__main__":
    apply_datetime_excepthook()
    main()
