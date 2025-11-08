from unittest.mock import patch

from no_tang_doc_agent.mcp_server import (
    FastMCPSettings,
    launch_mcp_server,
    register_mcp_prompts,
    register_mcp_resources,
)

from .helpers import setup_capture_prompt


def test_register_functions_are_callables():
    assert callable(register_mcp_prompts)
    assert callable(register_mcp_resources)


@patch("no_tang_doc_agent.mcp_server.server.FastMCP")
async def test_summarize_prompt_structure(mock_mcp):
    # Capture the prompt registered as summarize-document
    get_prompt = setup_capture_prompt(mock_mcp, "summarize-document")
    launch_mcp_server(
        base_url="http://test.example.com", mcp_settings=FastMCPSettings()
    )
    func = get_prompt()
    # Call the prompt function (synchronous)
    messages = func(document_id=42, length="short")
    assert isinstance(messages, list)
    # Expect two messages: instruction and resource reference
    assert any(isinstance(m, dict) and m.get("role") == "user" for m in messages)
    # Find resource message
    resource_msgs = [
        m
        for m in messages
        if isinstance(m, dict)
        and m.get("content")
        and isinstance(m["content"], dict)
        and m["content"].get("type") == "resource"
    ]
    assert len(resource_msgs) == 1
    res = resource_msgs[0]["content"]["resource"]
    assert res["uri"] == "resource://document/42"


@patch("no_tang_doc_agent.mcp_server.server.FastMCP")
async def test_create_team_prompt_structure(mock_mcp):
    get_prompt = setup_capture_prompt(mock_mcp, "create-team-prompt")
    launch_mcp_server(
        base_url="http://test.example.com", mcp_settings=FastMCPSettings()
    )
    func = get_prompt()
    msgs = func()
    assert isinstance(msgs, list)
    # Should request a suggested team name when none provided
    assert any(
        "suggest a concise team name" in (m.get("content") or "")
        for m in msgs
        if m.get("role") == "user"
    )


@patch("no_tang_doc_agent.mcp_server.server.FastMCP")
async def test_create_team_prompt_with_values(mock_mcp):
    # When name and description are provided, the prompt should include them
    get_prompt = setup_capture_prompt(mock_mcp, "create-team-prompt")
    launch_mcp_server(
        base_url="http://test.example.com", mcp_settings=FastMCPSettings()
    )
    func = get_prompt()
    msgs = func(name="TeamX", description="A nice team")
    assert any(
        "Proposed team name: TeamX" in (m.get("content") or "")
        for m in msgs
        if m.get("role") == "user"
    )
    assert any(
        "Proposed description: A nice team" in (m.get("content") or "")
        for m in msgs
        if m.get("role") == "user"
    )
