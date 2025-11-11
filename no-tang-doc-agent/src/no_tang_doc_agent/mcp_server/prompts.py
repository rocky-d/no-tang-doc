from typing import Any

from mcp.server.fastmcp import FastMCP

__all__ = [
    "register_mcp_prompts",
]


def register_mcp_prompts(
    mcp: FastMCP,
) -> None:
    @mcp.prompt(
        name="summarize-document",
        title="Summarize Document",
        description="Prompt to ask the model to summarize a document referenced by a resource URI.",
    )
    def summarize_document(
        document_id: int,
        length: str | None = "short",
    ) -> Any:
        """Return a prompt that instructs the model to summarize the document resource.

        The prompt references the `resource://document/{document_id}` resource so
        clients or the model can load the document content via MCP resources.
        """
        return [
            {
                "role": "user",
                "content": f"Please provide a {length} summary of the following document.",
            },
            {
                "role": "user",
                "content": {
                    "type": "resource",
                    "resource": {"uri": f"resource://document/{document_id}"},
                },
            },
        ]

    @mcp.prompt(
        name="create-team-prompt",
        title="Create Team Prompt",
        description="Interactive prompt template to gather info for creating a team.",
    )
    def create_team_prompt(
        name: str | None = None,
        description: str | None = None,
    ) -> Any:
        """Return a prompt that asks the model/user to confirm or generate team details.

        This prompt can be used to elicit a human-friendly team name and description
        before calling the `create-team` tool.
        """
        return [
            {
                "role": "user",
                "content": "Please suggest a concise team name."
                if name is None
                else f"Proposed team name: {name}",
            },
            {
                "role": "user",
                "content": "Please suggest a short description for the team."
                if description is None
                else f"Proposed description: {description}",
            },
            {
                "role": "assistant",
                "content": "Please confirm or improve the above and return final name and description.",
            },
        ]
