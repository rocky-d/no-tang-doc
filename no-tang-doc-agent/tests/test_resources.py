from unittest.mock import Mock, patch

import pytest

from no_tang_doc_agent.mcp_server import FastMCPSettings, launch_mcp_server

from .helpers import create_mock_client, make_api_client_factory, setup_capture_resource


@patch("no_tang_doc_agent.mcp_server.server.FastMCP")
@patch("no_tang_doc_agent.mcp_server.server.APIClient")
@patch("no_tang_doc_agent.mcp_server.resources.APIClient")
async def test_get_document_resource_success(
    mock_resources_api_client, mock_server_api_client, mock_mcp
):
    url = "http://test.example.com"
    metadata_client, metadata_response = create_mock_client(
        json_data={"data": {"downloadUrl": "http://cdn.example.com/file.txt"}}
    )
    content_client, content_response = create_mock_client(content=b"file content here")

    # Single proxied API client: first .get returns metadata, second returns content
    client, _ = create_mock_client()
    client.get.side_effect = [metadata_response, content_response]
    factory = make_api_client_factory(client)
    mock_resources_api_client.side_effect = factory
    mock_server_api_client.side_effect = factory

    get_resource = setup_capture_resource(mock_mcp, "resource://document/{document_id}")
    launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
    func = get_resource()
    # Create a mock ctx with request headers
    ctx = Mock()
    ctx.request_context.request.headers = {"authorization": "Bearer test-token"}
    ctx.fastmcp = Mock()

    result = await func(456, ctx)

    # Both requests are proxied through the same mocked client instance.
    assert client.get.call_args_list == [
        ((f"{url}/api/v1/documents/download/456",), {}),
        (("http://cdn.example.com/file.txt",), {}),
    ]
    assert result == b"file content here"


@patch("no_tang_doc_agent.mcp_server.server.FastMCP")
@patch("no_tang_doc_agent.mcp_server.server.APIClient")
@patch("no_tang_doc_agent.mcp_server.resources.APIClient")
async def test_get_document_resource_no_download_url(
    mock_resources_api_client, mock_server_api_client, mock_mcp
):
    url = "http://test.example.com"
    metadata_client, metadata_response = create_mock_client(json_data={})
    # Proxy APIClient so relative URLs are resolved
    factory = make_api_client_factory(metadata_client)
    mock_resources_api_client.side_effect = factory
    mock_server_api_client.side_effect = factory

    get_resource = setup_capture_resource(mock_mcp, "resource://document/{document_id}")
    launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
    func = get_resource()
    ctx = Mock()
    ctx.request_context.request.headers = {"authorization": "Bearer test-token"}
    ctx.fastmcp = Mock()

    with pytest.raises(ValueError):
        await func(123, ctx)
