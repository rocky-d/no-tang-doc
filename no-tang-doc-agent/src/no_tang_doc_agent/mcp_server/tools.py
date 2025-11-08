from typing import Any, Literal

from mcp import ServerSession
from mcp.server.fastmcp import Context, FastMCP

from .api_client import APIClient

__all__ = [
    "register_mcp_tools",
]


def register_mcp_tools(
    mcp: FastMCP,
) -> None:
    client: APIClient = APIClient()

    @mcp.tool(
        name="get-team-by-id",
        title="get-team-by-id",
        description="Fetch a team by its ID.",
    )
    async def get_api_v1_teams_teamid(
        ctx: Context[ServerSession, None],
        team_id: int,
    ) -> Any:
        resp = await client.get(
            f"/api/v1/teams/{team_id}",
            ctx=ctx,
        )
        resp.raise_for_status()
        return resp.json()

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
        resp = await client.put(
            f"/api/v1/teams/{team_id}",
            ctx=ctx,
            json={
                "name": name,
                "description": description,
            },
        )
        resp.raise_for_status()
        return resp.json()

    @mcp.tool(
        name="delete-team-by-id",
        title="delete-team-by-id",
        description="Delete a team by its ID.",
    )
    async def delete_api_v1_teams_teamid(
        ctx: Context[ServerSession, None],
        team_id: int,
    ) -> Any:
        resp = await client.delete(
            f"/api/v1/teams/{team_id}",
            ctx=ctx,
        )
        resp.raise_for_status()
        return resp.json()

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
        resp = await client.get(
            "/api/v1/teams",
            ctx=ctx,
            params=params,
        )
        resp.raise_for_status()
        return resp.json()

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
        resp = await client.post(
            "/api/v1/teams",
            ctx=ctx,
            json={
                "name": name,
                "description": description,
            },
        )
        resp.raise_for_status()
        return resp.json()

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
        resp = await client.put(
            f"/api/v1/teams/{team_id}/members/{member_id}",
            ctx=ctx,
            json={
                "role": role,
            },
        )
        resp.raise_for_status()
        return resp.json()

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
        resp = await client.delete(
            f"/api/v1/teams/{team_id}/members/{member_id}",
            ctx=ctx,
        )
        resp.raise_for_status()
        return resp.json()

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
        resp = await client.get(
            f"/api/v1/teams/{team_id}/members",
            ctx=ctx,
            params=params,
        )
        resp.raise_for_status()
        return resp.json()

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
        resp = await client.post(
            f"/api/v1/teams/{team_id}/members",
            ctx=ctx,
            json={
                "userKcId": user_kc_id,
                "role": role,
            },
        )
        resp.raise_for_status()
        return resp.json()

    @mcp.tool(
        name="leave-team",
        title="leave-team",
        description="Leave a team.",
    )
    async def post_api_v1_team_members_leave(
        ctx: Context[ServerSession, None],
        team_id: int,
    ) -> Any:
        resp = await client.post(
            f"/api/v1/teams/{team_id}/members/leave",
            ctx=ctx,
        )
        resp.raise_for_status()
        return resp.json()

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
        resp = await client.post(
            "/api/v1/documents/upload",
            ctx=ctx,
            params=params,
            files={"file": file_content.encode()},
        )
        resp.raise_for_status()
        return resp.json()

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
        resp = await client.get(
            "/api/v1/documents",
            ctx=ctx,
            params=params,
        )
        resp.raise_for_status()
        return resp.json()

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
        resp = await client.get(
            "/api/v1/documents/share",
            ctx=ctx,
            params=params,
        )
        resp.raise_for_status()
        return resp.json()

    @mcp.tool(
        name="download-document-metadata",
        title="download-document-metadata",
        description="Download metadata for a document.",
    )
    async def get_api_v1_documents_download_documentid(
        ctx: Context[ServerSession, None],
        document_id: int,
    ) -> Any:
        resp = await client.get(
            f"/api/v1/documents/download/{document_id}",
            ctx=ctx,
        )
        resp.raise_for_status()
        return resp.json()

    @mcp.tool(
        name="download-document-content",
        title="download-document-content",
        description="Download the content of a document.",
    )
    async def get_api_v1_documents_download_documentid__content(
        ctx: Context[ServerSession, None],
        document_id: int,
    ) -> Any:
        resp = await client.get(
            f"/api/v1/documents/download/{document_id}",
            ctx=ctx,
        )
        resp.raise_for_status()
        download_url = resp.json().get("data", {}).get("downloadUrl")
        if not download_url:
            raise ValueError("No download URL returned from core API")
        resp = await client.get(
            download_url,
        )
        resp.raise_for_status()
        return resp.content

    @mcp.tool(
        name="delete-document",
        title="delete-document",
        description="Delete a document.",
    )
    async def delete_api_v1_documents_documentid(
        ctx: Context[ServerSession, None],
        document_id: int,
    ) -> Any:
        resp = await client.delete(
            f"/api/v1/documents/{document_id}",
            ctx=ctx,
        )
        resp.raise_for_status()
        return resp.json()

    @mcp.tool(
        name="get-logs-list",
        title="get-logs-list",
        description="Fetch a list of logs.",
    )
    async def get_api_v1_logs_list(
        ctx: Context[ServerSession, None],
    ) -> Any:
        resp = await client.get(
            "/api/v1/logs/list",
            ctx=ctx,
        )
        resp.raise_for_status()
        return resp.json()

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
        resp = await client.get(
            "/api/v1/logs/documents",
            ctx=ctx,
            params=params,
        )
        resp.raise_for_status()
        return resp.json()

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
        resp = await client.post(
            "/api/v1/logs/count",
            ctx=ctx,
            params=params,
        )
        resp.raise_for_status()
        return resp.json()

    @mcp.tool(
        name="get-api-auth-me",
        title="get-api-auth-me",
        description="Fetch information about the authenticated user.",
    )
    async def get_api_auth_me(
        ctx: Context[ServerSession, None],
    ) -> Any:
        resp = await client.get(
            "/api/auth/me",
            ctx=ctx,
        )
        resp.raise_for_status()
        return resp.json()
