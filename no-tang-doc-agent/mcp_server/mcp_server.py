import httpx
from pydantic import AnyHttpUrl
from typing import Any, Literal
from mcp import ServerSession
from mcp.server.fastmcp import Context, FastMCP
from mcp.server.auth.provider import AccessToken, TokenVerifier
from mcp.server.auth.settings import AuthSettings

__all__ = [
    "mcp",
]

BASE_URL = "https://api.ntdoc.site"


class JWTTokenVerifier(TokenVerifier):
    async def verify_token(self, token: str) -> AccessToken:
        return AccessToken(
            token=token,
            client_id="no-tang-doc-mcp",
            scopes=["mcp-user"],
        )


mcp = FastMCP(
    name="no-tang-doc-agent-mcp-server",
    instructions="",
    debug=True,
    log_level="INFO",
    host="localhost",
    port=8001,
    token_verifier=JWTTokenVerifier(),
    auth=AuthSettings(
        issuer_url=AnyHttpUrl("https://auth.ntdoc.site/realms/ntdoc"),
        resource_server_url=AnyHttpUrl("http://localhost:8001/mcp"),
        required_scopes=["mcp-user"],
    ),
)


@mcp.tool()
async def get_api_v1_teams_teamid(
    ctx: Context[ServerSession, None],
    team_id: int,
) -> Any:
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.get(f"{BASE_URL}/api/v1/teams/{team_id}")
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def put_api_v1_teams_teamid(
    ctx: Context[ServerSession, None],
    team_id: int,
    name: str,
    description: str,
) -> Any:
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.put(
            f"{BASE_URL}/api/v1/teams/{team_id}",
            json={
                "name": name,
                "description": description,
            },
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def delete_api_v1_teams_teamid(
    ctx: Context[ServerSession, None],
    team_id: int,
) -> Any:
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.delete(
            f"{BASE_URL}/api/v1/teams/{team_id}",
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def get_api_v1_teams(
    ctx: Context[ServerSession, None],
    active_only: bool | None = None,
) -> Any:
    params = {}
    if active_only is not None:
        params["activeOnly"] = active_only
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.get(
            f"{BASE_URL}/api/v1/teams",
            params=params,
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def post_api_v1_teams(
    ctx: Context[ServerSession, None],
    name: str,
    description: str,
) -> Any:
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.post(
            f"{BASE_URL}/api/v1/teams",
            json={
                "name": name,
                "description": description,
            },
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def put_api_v1_teams_teamid_members_memberid(
    ctx: Context[ServerSession, None],
    team_id: int,
    member_id: int,
    role: str,
) -> Any:
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.put(
            f"{BASE_URL}/api/v1/teams/{team_id}/members/{member_id}",
            json={
                "role": role,
            },
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def delete_api_v1_teams_teamid_members_memberid(
    ctx: Context[ServerSession, None],
    team_id: int,
    member_id: int,
) -> Any:
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.delete(
            f"{BASE_URL}/api/v1/teams/{team_id}/members/{member_id}",
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def get_api_v1_team_members(
    ctx: Context[ServerSession, None],
    team_id: int,
    active_only: bool | None = None,
) -> Any:
    params = {}
    if active_only is not None:
        params["activeOnly"] = active_only
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.get(
            f"{BASE_URL}/api/v1/teams/{team_id}/members",
            params=params,
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def post_api_v1_team_members(
    ctx: Context[ServerSession, None],
    team_id: int,
    user_kc_id: int,
    role: str,
) -> Any:
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.post(
            f"{BASE_URL}/api/v1/teams/{team_id}/members",
            json={
                "userKcId": user_kc_id,
                "role": role,
            },
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def post_api_v1_team_members_leave(
    ctx: Context[ServerSession, None],
    team_id: int,
) -> Any:
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.post(
            f"{BASE_URL}/api/v1/teams/{team_id}/members/leave",
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
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
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.post(
            f"{BASE_URL}/api/v1/documents/upload",
            params=params,
            files={"file": file_content.encode()},
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def get_api_v1_documents(
    ctx: Context[ServerSession, None],
    status: Literal["UPLOADING", "ACTIVE", "DELETED", "PROCESSING"] | None = None,
) -> Any:
    params = {}
    if status is not None:
        params["status"] = status
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.get(
            f"{BASE_URL}/api/v1/documents",
            params=params,
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def get_api_v1_documents_share(
    ctx: Context[ServerSession, None],
    document_id: int,
    expiration_minutes: int | None = None,
) -> Any:
    params = {}
    params["documentId"] = document_id
    if expiration_minutes is not None:
        params["expirationMinutes"] = expiration_minutes
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.get(
            f"{BASE_URL}/api/v1/documents/share",
            params=params,
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def get_api_v1_documents_download_documentid(
    ctx: Context[ServerSession, None],
    document_id: int,
) -> Any:
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.get(
            f"{BASE_URL}/api/v1/documents/download/{document_id}",
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def delete_api_v1_documents_documentid(
    ctx: Context[ServerSession, None],
    document_id: int,
) -> Any:
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.delete(
            f"{BASE_URL}/api/v1/documents/{document_id}",
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def get_api_v1_logs_list(
    ctx: Context[ServerSession, None],
) -> Any:
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.get(
            f"{BASE_URL}/api/v1/logs/list",
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def get_api_v1_logs_documents(
    ctx: Context[ServerSession, None],
    document_id: int,
) -> Any:
    params = {}
    params["documentId"] = document_id
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.get(
            f"{BASE_URL}/api/v1/logs/documents",
            params=params,
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def post_api_v1_logs_count(
    ctx: Context[ServerSession, None],
    period: Literal["week", "month"] | None = None,
) -> Any:
    params = {}
    if period is not None:
        params["period"] = period
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.post(
            f"{BASE_URL}/api/v1/logs/count",
            params=params,
        )
        response.raise_for_status()
        return response.json()


@mcp.tool()
async def get_api_auth_me(
    ctx: Context[ServerSession, None],
) -> Any:
    async with httpx.AsyncClient(
        headers={"Authorization": ctx.request_context.request.headers["authorization"]},
    ) as client:
        response = await client.get(
            f"{BASE_URL}/api/auth/me",
        )
        response.raise_for_status()
        return response.json()
