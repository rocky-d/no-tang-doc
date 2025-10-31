import click
from mcp.server.auth.settings import AuthSettings
from pydantic import AnyHttpUrl

from no_tang_doc_agent.datetime_excepthook import apply_datetime_excepthook
from no_tang_doc_agent.mcp_server import (
    FastMCPSettings,
    JWTTokenVerifier,
    start_mcp_server,
)


@click.command()
@click.option(
    "--base-url",
    default="http://localhost:8070",
    help="Base URL for the MCP server",
    show_default=True,
)
@click.option(
    "--name",
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
    default="INFO",
    type=click.Choice(
        ["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"], case_sensitive=False
    ),
    help="Logging level",
    show_default=True,
)
@click.option(
    "--host",
    default="localhost",
    help="Host address to bind the server",
    show_default=True,
)
@click.option(
    "--port",
    default=8002,
    type=int,
    help="Port number to bind the server",
    show_default=True,
)
@click.option(
    "--issuer-url",
    default="http://auth.local:8080/realms/ntdoc",
    help="OAuth2/OIDC issuer URL",
    show_default=True,
)
@click.option(
    "--required-scopes",
    multiple=True,
    default=["email", "profile", "mcp-user"],
    help="Required OAuth2 scopes (can be specified multiple times)",
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
    required_scopes: tuple[str, ...],
) -> None:
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
                resource_server_url=AnyHttpUrl(f"http://{host}:{port}/mcp"),
                required_scopes=list(required_scopes),
            ),
        ),
    )


if __name__ == "__main__":
    apply_datetime_excepthook()
    main()
