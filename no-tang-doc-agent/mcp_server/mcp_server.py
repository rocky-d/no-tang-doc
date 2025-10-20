import httpx
from mcp.server.fastmcp import FastMCP

__all__ = [
    "mcp",
]

BASE_URL = "http://localhost:8000"


mcp = FastMCP(
    name="no-tang-doc-agent-mcp-server",
    instructions="",
    debug=True,
    log_level="INFO",
    host="localhost",
    port=8001,
)


@mcp.tool()
async def echo(
    message: str,
) -> str:
    return message


@mcp.tool()
async def api_user_me() -> dict:
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{BASE_URL}/api/user/me")
        response.raise_for_status()
        return response.json()
