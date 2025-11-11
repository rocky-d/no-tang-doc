from typing import Any

from mcp import ServerSession
from mcp.server.fastmcp import Context, FastMCP

from .api_client import APIClient

__all__ = [
    "register_mcp_resources",
]


def register_mcp_resources(
    mcp: FastMCP,
) -> None:
    client: APIClient = APIClient()

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
