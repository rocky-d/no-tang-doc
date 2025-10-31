from collections.abc import Callable, Collection
from contextlib import AbstractAsyncContextManager
from dataclasses import dataclass
from typing import Any, Literal

import httpx
import jwt
from mcp import ServerSession
from mcp.server.auth.provider import (
    AccessToken,
    OAuthAuthorizationServerProvider,
    TokenVerifier,
)
from mcp.server.auth.settings import AuthSettings
from mcp.server.fastmcp import Context, FastMCP
from mcp.server.fastmcp.tools import Tool
from mcp.server.lowlevel.server import LifespanResultT
from mcp.server.streamable_http import EventStore
from mcp.server.transport_security import TransportSecuritySettings
from mcp.types import Icon

__all__ = [
    "JWTTokenVerifier",
    "FastMCPSettings",
    "start_mcp_server",
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
        resource = " ".join(_aud)
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


def start_mcp_server(
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

    @mcp.tool(
        name="get-team-by-id",
        title="get-team-by-id",
        description="Fetch a team by its ID.",
    )
    async def get_api_v1_teams_teamid(
        ctx: Context[ServerSession, None],
        team_id: int,
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization},
        ) as client:
            response = await client.get(f"{base_url}/api/v1/teams/{team_id}")
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="update-team-by-id",
        title="update-team-by-id",
        description="Update a team's information by its ID.",
    )
    async def put_api_v1_teams_teamid(
        ctx: Context[ServerSession, None],
        team_id: int,
        name: str,
        description: str,
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization},
        ) as client:
            response = await client.put(
                f"{base_url}/api/v1/teams/{team_id}",
                json={
                    "name": name,
                    "description": description,
                },
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="delete-team-by-id",
        title="delete-team-by-id",
        description="Delete a team by its ID.",
    )
    async def delete_api_v1_teams_teamid(
        ctx: Context[ServerSession, None],
        team_id: int,
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.delete(
                f"{base_url}/api/v1/teams/{team_id}",
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="get-teams",
        title="get-teams",
        description="Fetch a list of teams.",
    )
    async def get_api_v1_teams(
        ctx: Context[ServerSession, None],
        active_only: bool | None = None,
    ) -> Any:
        params = {}
        if active_only is not None:
            params["activeOnly"] = active_only
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.get(
                f"{base_url}/api/v1/teams",
                params=params,
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="create-team",
        title="create-team",
        description="Create a new team.",
    )
    async def post_api_v1_teams(
        ctx: Context[ServerSession, None],
        name: str,
        description: str,
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.post(
                f"{base_url}/api/v1/teams",
                json={
                    "name": name,
                    "description": description,
                },
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="update-team-member-role",
        title="update-team-member-role",
        description="Update a team member's role.",
    )
    async def put_api_v1_teams_teamid_members_memberid(
        ctx: Context[ServerSession, None],
        team_id: int,
        member_id: int,
        role: str,
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.put(
                f"{base_url}/api/v1/teams/{team_id}/members/{member_id}",
                json={
                    "role": role,
                },
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="remove-team-member",
        title="remove-team-member",
        description="Remove a member from a team.",
    )
    async def delete_api_v1_teams_teamid_members_memberid(
        ctx: Context[ServerSession, None],
        team_id: int,
        member_id: int,
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.delete(
                f"{base_url}/api/v1/teams/{team_id}/members/{member_id}",
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="get-team-members",
        title="get-team-members",
        description="Fetch members of a team.",
    )
    async def get_api_v1_team_members(
        ctx: Context[ServerSession, None],
        team_id: int,
        active_only: bool | None = None,
    ) -> Any:
        params = {}
        if active_only is not None:
            params["activeOnly"] = active_only
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.get(
                f"{base_url}/api/v1/teams/{team_id}/members",
                params=params,
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="add-team-member",
        title="add-team-member",
        description="Add a member to a team.",
    )
    async def post_api_v1_team_members(
        ctx: Context[ServerSession, None],
        team_id: int,
        user_kc_id: int,
        role: str,
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.post(
                f"{base_url}/api/v1/teams/{team_id}/members",
                json={
                    "userKcId": user_kc_id,
                    "role": role,
                },
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="leave-team",
        title="leave-team",
        description="Leave a team.",
    )
    async def post_api_v1_team_members_leave(
        ctx: Context[ServerSession, None],
        team_id: int,
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.post(
                f"{base_url}/api/v1/teams/{team_id}/members/leave",
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="upload-document",
        title="upload-document",
        description="Upload a document.",
    )
    async def post_api_v1_documents_upload(
        ctx: Context[ServerSession, None],
        file_content: str,
        file_name: str | None = None,
        description: str | None = None,
    ) -> Any:
        params = {}
        if file_name is not None:
            params["fileName"] = file_name
        if description is not None:
            params["description"] = description
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.post(
                f"{base_url}/api/v1/documents/upload",
                params=params,
                files={"file": file_content.encode()},
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="get-documents",
        title="get-documents",
        description="Fetch a list of documents.",
    )
    async def get_api_v1_documents(
        ctx: Context[ServerSession, None],
        status: Literal["UPLOADING", "ACTIVE", "DELETED", "PROCESSING"] | None = None,
    ) -> Any:
        params = {}
        if status is not None:
            params["status"] = status
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.get(
                f"{base_url}/api/v1/documents",
                params=params,
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="share-document",
        title="share-document",
        description="Generate a shareable link for a document.",
    )
    async def get_api_v1_documents_share(
        ctx: Context[ServerSession, None],
        document_id: int,
        expiration_minutes: int | None = None,
    ) -> Any:
        params = {}
        params["documentId"] = document_id
        if expiration_minutes is not None:
            params["expirationMinutes"] = expiration_minutes
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.get(
                f"{base_url}/api/v1/documents/share",
                params=params,
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="download-document-metadata",
        title="download-document-metadata",
        description="Download metadata for a document.",
    )
    async def get_api_v1_documents_download_documentid(
        ctx: Context[ServerSession, None],
        document_id: int,
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.get(
                f"{base_url}/api/v1/documents/download/{document_id}",
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="download-document-content",
        title="download-document-content",
        description="Download the content of a document.",
    )
    async def get_api_v1_documents_download_documentid__content(
        ctx: Context[ServerSession, None],
        document_id: int,
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.get(
                f"{base_url}/api/v1/documents/download/{document_id}",
            )
            response.raise_for_status()
            download_url = response.json()["data"]["downloadUrl"]
            async with httpx.AsyncClient() as client:
                response = await client.get(download_url)
                response.raise_for_status()
                return response.content

    @mcp.tool(
        name="delete-document",
        title="delete-document",
        description="Delete a document.",
    )
    async def delete_api_v1_documents_documentid(
        ctx: Context[ServerSession, None],
        document_id: int,
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.delete(
                f"{base_url}/api/v1/documents/{document_id}",
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="get-logs-list",
        title="get-logs-list",
        description="Fetch a list of logs.",
    )
    async def get_api_v1_logs_list(
        ctx: Context[ServerSession, None],
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.get(
                f"{base_url}/api/v1/logs/list",
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="get-logs-documents",
        title="get-logs-documents",
        description="Fetch log documents by document ID.",
    )
    async def get_api_v1_logs_documents(
        ctx: Context[ServerSession, None],
        document_id: int,
    ) -> Any:
        params = {}
        params["documentId"] = document_id
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.get(
                f"{base_url}/api/v1/logs/documents",
                params=params,
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="get-logs-count",
        title="get-logs-count",
        description="Fetch the count of logs over a specified period.",
    )
    async def post_api_v1_logs_count(
        ctx: Context[ServerSession, None],
        period: Literal["week", "month"] | None = None,
    ) -> Any:
        params = {}
        if period is not None:
            params["period"] = period
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.post(
                f"{base_url}/api/v1/logs/count",
                params=params,
            )
            response.raise_for_status()
            return response.json()

    @mcp.tool(
        name="get-api-auth-me",
        title="get-api-auth-me",
        description="Fetch information about the authenticated user.",
    )
    async def get_api_auth_me(
        ctx: Context[ServerSession, None],
    ) -> Any:
        authorization = ctx.request_context.request.headers["authorization"]
        async with httpx.AsyncClient(
            headers={"Authorization": authorization}
        ) as client:
            response = await client.get(
                f"{base_url}/api/auth/me",
            )
            response.raise_for_status()
            return response.json()

    mcp.run(transport=transport, mount_path=mount_path)
