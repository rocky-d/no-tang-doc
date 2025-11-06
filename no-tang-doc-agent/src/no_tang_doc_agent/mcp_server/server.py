from collections.abc import Callable, Collection
from contextlib import AbstractAsyncContextManager
from dataclasses import dataclass
from typing import Any, Literal

import jwt
from mcp.server.auth.provider import (
    AccessToken,
    OAuthAuthorizationServerProvider,
    TokenVerifier,
)
from mcp.server.auth.settings import AuthSettings
from mcp.server.fastmcp import FastMCP
from mcp.server.fastmcp.tools import Tool
from mcp.server.lowlevel.server import LifespanResultT
from mcp.server.streamable_http import EventStore
from mcp.server.transport_security import TransportSecuritySettings
from mcp.types import Icon

from .tools import register_mcp_tools

__all__ = [
    "JWTTokenVerifier",
    "FastMCPSettings",
    "launch_mcp_server",
]


class JWTTokenVerifier(TokenVerifier):
    async def verify_token(
        self,
        token: str,
    ) -> AccessToken | None:
        _payload = jwt.decode(token, options={"verify_signature": False})
        client_id = _payload.get("azp")
        if client_id is None:
            return None
        _scope = _payload.get("scope")
        if _scope is None:
            return None
        scopes = _scope.split() if _scope else []
        expires_at = _payload.get("exp")
        if expires_at is None:
            return None
        resource = None
        _aud = _payload.get("aud")
        if _aud is None:
            return None
        # Handle aud as either a string or a list of strings
        resource = " ".join(_aud) if isinstance(_aud, list) else str(_aud)
        return AccessToken(
            token=token,
            client_id=client_id,
            scopes=scopes,
            expires_at=expires_at,
            resource=resource,
        )


@dataclass
class FastMCPSettings:
    name: str | None = None
    instructions: str | None = None
    website_url: str | None = None
    icons: list[Icon] | None = None
    auth_server_provider: OAuthAuthorizationServerProvider[Any, Any, Any] | None = None
    token_verifier: TokenVerifier | None = None
    event_store: EventStore | None = None
    tools: list[Tool] | None = None
    debug: bool = False
    log_level: Literal["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"] = "INFO"
    host: str = "127.0.0.1"
    port: int = 8000
    mount_path: str = "/"
    sse_path: str = "/sse"
    message_path: str = "/messages/"
    streamable_http_path: str = "/mcp"
    json_response: bool = False
    stateless_http: bool = False
    warn_on_duplicate_resources: bool = True
    warn_on_duplicate_tools: bool = True
    warn_on_duplicate_prompts: bool = True
    dependencies: Collection[str] = ()
    lifespan: (
        Callable[
            [FastMCP[LifespanResultT]], AbstractAsyncContextManager[LifespanResultT]
        ]
        | None
    ) = None
    auth: AuthSettings | None = None
    transport_security: TransportSecuritySettings | None = None


def launch_mcp_server(
    base_url: str = "https://api.ntdoc.site",
    mcp_settings: FastMCPSettings | None = None,
    transport: Literal["streamable-http"] = "streamable-http",
    mount_path: str | None = None,
) -> None:
    if mcp_settings is None:
        mcp_settings = FastMCPSettings()
    mcp = FastMCP(
        name=mcp_settings.name,
        instructions=mcp_settings.instructions,
        website_url=mcp_settings.website_url,
        icons=mcp_settings.icons,
        auth_server_provider=mcp_settings.auth_server_provider,
        token_verifier=mcp_settings.token_verifier,
        event_store=mcp_settings.event_store,
        tools=mcp_settings.tools,
        debug=mcp_settings.debug,
        log_level=mcp_settings.log_level,
        host=mcp_settings.host,
        port=mcp_settings.port,
        mount_path=mcp_settings.mount_path,
        sse_path=mcp_settings.sse_path,
        message_path=mcp_settings.message_path,
        streamable_http_path=mcp_settings.streamable_http_path,
        json_response=mcp_settings.json_response,
        stateless_http=mcp_settings.stateless_http,
        warn_on_duplicate_resources=mcp_settings.warn_on_duplicate_resources,
        warn_on_duplicate_tools=mcp_settings.warn_on_duplicate_tools,
        warn_on_duplicate_prompts=mcp_settings.warn_on_duplicate_prompts,
        dependencies=mcp_settings.dependencies,
        lifespan=mcp_settings.lifespan,
        auth=mcp_settings.auth,
        transport_security=mcp_settings.transport_security,
    )
    register_mcp_tools(mcp, base_url=base_url)
    mcp.run(transport=transport, mount_path=mount_path)
