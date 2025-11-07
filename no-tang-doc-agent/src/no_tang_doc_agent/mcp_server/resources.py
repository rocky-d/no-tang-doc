from typing import Any

import httpx
from mcp import ServerSession
from mcp.server.fastmcp import Context, FastMCP

__all__ = [
    "register_mcp_resources",
]


def register_mcp_resources(
    mcp: FastMCP,
    *,
    base_url: str,
) -> None:
    @mcp.resource(
        "resource://document/{document_id}",
        description="Document content resource.",
    )
    async def get_document_resource(
        document_id: int,
        ctx: Context[ServerSession, None],
    ) -> Any:
        """Return the contents of a document identified by document_id.

        This mirrors the behavior of the existing download tool: it first
        queries the core API for a download URL and then fetches the content
        at that URL. Authorization headers are forwarded from the MCP request
        context.
        """
        authorization = ctx.request_context.request.headers.get("authorization")

        async with httpx.AsyncClient(
            headers={"Authorization": authorization},
        ) as client:
            response = await client.get(
                f"{base_url}/api/v1/documents/download/{document_id}"
            )
            response.raise_for_status()
            data = response.json()
            download_url = data.get("data", {}).get("downloadUrl")
            if not download_url:
                raise ValueError("No download URL returned from core API")

        async with httpx.AsyncClient() as client:
            resp = await client.get(download_url)
            resp.raise_for_status()
            return resp.content
