from unittest.mock import Mock, patch

import pytest

from no_tang_doc_agent.mcp_server import FastMCPSettings, launch_mcp_server

from .test_utils import create_mock_client, setup_capture_resource


@patch("no_tang_doc_agent.mcp_server.server.FastMCP")
@patch("no_tang_doc_agent.mcp_server.resources.httpx.AsyncClient")
async def test_get_document_resource_success(mock_httpx, mock_mcp):
    url = "http://test.example.com"
    # metadata client returns a downloadUrl
    metadata_client, metadata_response = create_mock_client(
        json_data={"data": {"downloadUrl": "http://cdn.example.com/file.txt"}}
    )
    # content client returns bytes content
    content_client, content_response = create_mock_client(content=b"file content here")

    mock_httpx.side_effect = [metadata_client, content_client]

    get_resource = setup_capture_resource(mock_mcp, "resource://document/{document_id}")
    launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
    func = get_resource()
    # Create a mock ctx with request headers
    ctx = Mock()
    ctx.request_context.request.headers = {"authorization": "Bearer test-token"}
    ctx.fastmcp = Mock()

    result = await func(456, ctx)

    metadata_client.get.assert_called_once_with(f"{url}/api/v1/documents/download/456")
    content_client.get.assert_called_once_with("http://cdn.example.com/file.txt")
    assert result == b"file content here"


@patch("no_tang_doc_agent.mcp_server.server.FastMCP")
@patch("no_tang_doc_agent.mcp_server.resources.httpx.AsyncClient")
async def test_get_document_resource_no_download_url(mock_httpx, mock_mcp):
    url = "http://test.example.com"
    metadata_client, metadata_response = create_mock_client(json_data={})
    mock_httpx.return_value = metadata_client

    get_resource = setup_capture_resource(mock_mcp, "resource://document/{document_id}")
    launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
    func = get_resource()
    ctx = Mock()
    ctx.request_context.request.headers = {"authorization": "Bearer test-token"}
    ctx.fastmcp = Mock()

    with pytest.raises(ValueError):
        await func(123, ctx)
