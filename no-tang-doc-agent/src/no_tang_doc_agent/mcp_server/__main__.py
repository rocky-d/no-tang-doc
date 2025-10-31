from mcp.server.auth.settings import AuthSettings
from pydantic import AnyHttpUrl

from no_tang_doc_agent.datetime_excepthook import apply_datetime_excepthook
from no_tang_doc_agent.mcp_server import (
    FastMCPSettings,
    JWTTokenVerifier,
    start_mcp_server,
)

if __name__ == "__main__":
    apply_datetime_excepthook()
    start_mcp_server(
        base_url="http://localhost:8070",
        mcp_settings=FastMCPSettings(
            name="no-tang-doc-agent-mcp-server",
            debug=True,
            log_level="INFO",
            host="localhost",
            port=8001,
            token_verifier=JWTTokenVerifier(),
            auth=AuthSettings(
                issuer_url=AnyHttpUrl("http://auth.local:8080/realms/ntdoc"),
                resource_server_url=AnyHttpUrl("http://localhost:8001/mcp"),
                required_scopes=["mcp-user"],
            ),
        ),
    )
