from unittest.mock import AsyncMock, MagicMock, Mock, patch

import pytest

from no_tang_doc_agent.mcp_server import (
    FastMCPSettings,
    JWTTokenVerifier,
    start_mcp_server,
)


class TestJWTTokenVerifier:
    @pytest.fixture
    def verifier(self):
        return JWTTokenVerifier()

    async def test_verify_token_success(self, verifier):
        payload = {
            "azp": "test-client",
            "scope": "mcp-user admin",
            "exp": 1234567890,
            "aud": ["service1", "service2"],
        }
        with patch("jwt.decode", return_value=payload):
            result = await verifier.verify_token("test.token")
        assert result is not None
        assert result.client_id == "test-client"
        assert result.scopes == ["mcp-user", "admin"]
        assert result.resource == "service1 service2"

    async def test_verify_token_missing_client_id(self, verifier):
        payload = {"scope": "mcp-user", "exp": 1234567890, "aud": ["service1"]}
        with patch("jwt.decode", return_value=payload):
            result = await verifier.verify_token("test.token")
        assert result is None

    async def test_verify_token_missing_scope(self, verifier):
        payload = {"azp": "test-client", "exp": 1234567890, "aud": ["service1"]}
        with patch("jwt.decode", return_value=payload):
            result = await verifier.verify_token("test.token")
        assert result is None

    async def test_verify_token_missing_exp(self, verifier):
        payload = {"azp": "test-client", "scope": "mcp-user", "aud": ["service1"]}
        with patch("jwt.decode", return_value=payload):
            result = await verifier.verify_token("test.token")
        assert result is None

    async def test_verify_token_missing_aud(self, verifier):
        payload = {"azp": "test-client", "scope": "mcp-user", "exp": 1234567890}
        with patch("jwt.decode", return_value=payload):
            result = await verifier.verify_token("test.token")
        assert result is None

    async def test_verify_token_empty_scope(self, verifier):
        payload = {
            "azp": "test-client",
            "scope": "",
            "exp": 1234567890,
            "aud": ["service1"],
        }
        with patch("jwt.decode", return_value=payload):
            result = await verifier.verify_token("test.token")
        assert result is not None
        assert result.scopes == []


class TestFastMCPSettings:
    def test_default_values(self):
        s = FastMCPSettings()
        assert s.name is None
        assert s.debug is False
        assert s.log_level == "INFO"
        assert s.host == "127.0.0.1"
        assert s.port == 8000

    def test_custom_values(self):
        s = FastMCPSettings(name="test", debug=True, port=9000)
        assert s.name == "test"
        assert s.debug is True
        assert s.port == 9000


@pytest.fixture
def mock_context():
    ctx = MagicMock()
    ctx.request_context.request.headers = {"authorization": "Bearer test-token"}
    return ctx


def create_mock_client(json_data=None, content=None):
    client = AsyncMock()
    response = Mock()
    if json_data:
        response.json = Mock(return_value=json_data)
    if content:
        response.content = content
    response.raise_for_status = Mock()
    client.get = AsyncMock(return_value=response)
    client.post = AsyncMock(return_value=response)
    client.put = AsyncMock(return_value=response)
    client.delete = AsyncMock(return_value=response)
    client.__aenter__ = AsyncMock(return_value=client)
    client.__aexit__ = AsyncMock(return_value=None)
    return client, response


def setup_capture(mock_mcp, tool_name):
    captured = None

    def decorator(**kwargs):
        def wrapper(func):
            nonlocal captured
            if kwargs.get("name") == tool_name:
                captured = func
            return func

        return wrapper

    mock_mcp.return_value.tool.side_effect = decorator
    mock_mcp.return_value.run = Mock()
    return lambda: captured


class TestMCPTools:
    @pytest.fixture
    def url(self):
        return "http://test.example.com"

    # Team related tests
    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_get_team_by_id(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"id": 1, "name": "Test Team"})
        mock_httpx.return_value = client
        get = setup_capture(mock_mcp, "get-team-by-id")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await get()(mock_context, team_id=123)
        client.get.assert_called_once_with(f"{url}/api/v1/teams/123")
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_update_team_by_id(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"id": 1, "name": "Updated Team"})
        mock_httpx.return_value = client
        update = setup_capture(mock_mcp, "update-team-by-id")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await update()(
            mock_context, team_id=123, name="Updated Team", description="New desc"
        )
        client.put.assert_called_once_with(
            f"{url}/api/v1/teams/123",
            json={"name": "Updated Team", "description": "New desc"},
        )
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_delete_team_by_id(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"success": True})
        mock_httpx.return_value = client
        delete = setup_capture(mock_mcp, "delete-team-by-id")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await delete()(mock_context, team_id=123)
        client.delete.assert_called_once_with(f"{url}/api/v1/teams/123")
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_get_teams(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"teams": []})
        mock_httpx.return_value = client
        get = setup_capture(mock_mcp, "get-teams")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await get()(mock_context, active_only=True)
        client.get.assert_called_once_with(
            f"{url}/api/v1/teams", params={"activeOnly": True}
        )
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_get_teams_no_params(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"teams": []})
        mock_httpx.return_value = client
        get = setup_capture(mock_mcp, "get-teams")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await get()(mock_context)
        client.get.assert_called_once_with(f"{url}/api/v1/teams", params={})
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_create_team(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"id": 999, "name": "New Team"})
        mock_httpx.return_value = client
        create = setup_capture(mock_mcp, "create-team")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await create()(
            mock_context, name="New Team", description="Team description"
        )
        client.post.assert_called_once_with(
            f"{url}/api/v1/teams",
            json={"name": "New Team", "description": "Team description"},
        )
        response.raise_for_status.assert_called_once()

    # Team member related tests
    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_update_team_member_role(
        self, mock_httpx, mock_mcp, mock_context, url
    ):
        client, response = create_mock_client({"success": True})
        mock_httpx.return_value = client
        update = setup_capture(mock_mcp, "update-team-member-role")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await update()(mock_context, team_id=123, member_id=456, role="admin")
        client.put.assert_called_once_with(
            f"{url}/api/v1/teams/123/members/456", json={"role": "admin"}
        )
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_remove_team_member(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"success": True})
        mock_httpx.return_value = client
        remove = setup_capture(mock_mcp, "remove-team-member")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await remove()(mock_context, team_id=123, member_id=456)
        client.delete.assert_called_once_with(f"{url}/api/v1/teams/123/members/456")
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_get_team_members(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"members": []})
        mock_httpx.return_value = client
        get = setup_capture(mock_mcp, "get-team-members")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context, team_id=123, active_only=False)
        client.get.assert_called_once_with(
            f"{url}/api/v1/teams/123/members", params={"activeOnly": False}
        )
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_get_team_members_no_params(
        self, mock_httpx, mock_mcp, mock_context, url
    ):
        client, response = create_mock_client({"members": []})
        mock_httpx.return_value = client
        get = setup_capture(mock_mcp, "get-team-members")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context, team_id=123)
        client.get.assert_called_once_with(f"{url}/api/v1/teams/123/members", params={})
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_add_team_member(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"success": True})
        mock_httpx.return_value = client
        add = setup_capture(mock_mcp, "add-team-member")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await add()(mock_context, team_id=123, user_kc_id=789, role="member")
        client.post.assert_called_once_with(
            f"{url}/api/v1/teams/123/members", json={"userKcId": 789, "role": "member"}
        )
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_leave_team(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"success": True})
        mock_httpx.return_value = client
        leave = setup_capture(mock_mcp, "leave-team")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await leave()(mock_context, team_id=123)
        client.post.assert_called_once_with(f"{url}/api/v1/teams/123/members/leave")
        response.raise_for_status.assert_called_once()

    # Document related tests
    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_upload_document(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"id": 999, "status": "UPLOADING"})
        mock_httpx.return_value = client
        upload = setup_capture(mock_mcp, "upload-document")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await upload()(
            mock_context,
            file_content="test content",
            file_name="test.txt",
            description="Test doc",
        )
        client.post.assert_called_once_with(
            f"{url}/api/v1/documents/upload",
            params={"fileName": "test.txt", "description": "Test doc"},
            files={"file": b"test content"},
        )
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_upload_document_no_optional_params(
        self, mock_httpx, mock_mcp, mock_context, url
    ):
        client, response = create_mock_client({"id": 999})
        mock_httpx.return_value = client
        upload = setup_capture(mock_mcp, "upload-document")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await upload()(mock_context, file_content="test content")
        client.post.assert_called_once_with(
            f"{url}/api/v1/documents/upload", params={}, files={"file": b"test content"}
        )
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_get_documents(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"documents": []})
        mock_httpx.return_value = client
        get = setup_capture(mock_mcp, "get-documents")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context, status="ACTIVE")
        client.get.assert_called_once_with(
            f"{url}/api/v1/documents", params={"status": "ACTIVE"}
        )
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_get_documents_no_status(
        self, mock_httpx, mock_mcp, mock_context, url
    ):
        client, response = create_mock_client({"documents": []})
        mock_httpx.return_value = client
        get = setup_capture(mock_mcp, "get-documents")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context)
        client.get.assert_called_once_with(f"{url}/api/v1/documents", params={})
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_share_document(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"shareUrl": "http://share.link"})
        mock_httpx.return_value = client
        share = setup_capture(mock_mcp, "share-document")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await share()(mock_context, document_id=456, expiration_minutes=60)
        client.get.assert_called_once_with(
            f"{url}/api/v1/documents/share",
            params={"documentId": 456, "expirationMinutes": 60},
        )
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_share_document_no_expiration(
        self, mock_httpx, mock_mcp, mock_context, url
    ):
        client, response = create_mock_client({"shareUrl": "http://share.link"})
        mock_httpx.return_value = client
        share = setup_capture(mock_mcp, "share-document")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await share()(mock_context, document_id=456)
        client.get.assert_called_once_with(
            f"{url}/api/v1/documents/share", params={"documentId": 456}
        )
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_download_document_metadata(
        self, mock_httpx, mock_mcp, mock_context, url
    ):
        client, response = create_mock_client({"id": 456, "name": "doc.txt"})
        mock_httpx.return_value = client
        download = setup_capture(mock_mcp, "download-document-metadata")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await download()(mock_context, document_id=456)
        client.get.assert_called_once_with(f"{url}/api/v1/documents/download/456")
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_download_document_content(
        self, mock_httpx, mock_mcp, mock_context, url
    ):
        # Create first client for metadata request
        metadata_client, metadata_response = create_mock_client(
            {"data": {"downloadUrl": "http://cdn.example.com/file.txt"}}
        )
        # Create second client for content download
        content_client, content_response = create_mock_client(
            content=b"file content here"
        )

        # Setup mock to return different clients for each call
        mock_httpx.side_effect = [metadata_client, content_client]

        download = setup_capture(mock_mcp, "download-document-content")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await download()(mock_context, document_id=456)

        # Verify both calls
        metadata_client.get.assert_called_once_with(
            f"{url}/api/v1/documents/download/456"
        )
        content_client.get.assert_called_once_with("http://cdn.example.com/file.txt")
        assert result == b"file content here"

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_delete_document(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"success": True})
        mock_httpx.return_value = client
        delete = setup_capture(mock_mcp, "delete-document")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await delete()(mock_context, document_id=456)
        client.delete.assert_called_once_with(f"{url}/api/v1/documents/456")
        response.raise_for_status.assert_called_once()

    # Log related tests
    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_get_logs_list(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"logs": []})
        mock_httpx.return_value = client
        get = setup_capture(mock_mcp, "get-logs-list")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context)
        client.get.assert_called_once_with(f"{url}/api/v1/logs/list")
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_get_logs_documents(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"logs": []})
        mock_httpx.return_value = client
        get = setup_capture(mock_mcp, "get-logs-documents")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context, document_id=789)
        client.get.assert_called_once_with(
            f"{url}/api/v1/logs/documents", params={"documentId": 789}
        )
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_get_logs_count(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"count": 42})
        mock_httpx.return_value = client
        get = setup_capture(mock_mcp, "get-logs-count")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context, period="week")
        client.post.assert_called_once_with(
            f"{url}/api/v1/logs/count", params={"period": "week"}
        )
        response.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_get_logs_count_no_period(
        self, mock_httpx, mock_mcp, mock_context, url
    ):
        client, response = create_mock_client({"count": 100})
        mock_httpx.return_value = client
        get = setup_capture(mock_mcp, "get-logs-count")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context)
        client.post.assert_called_once_with(f"{url}/api/v1/logs/count", params={})
        response.raise_for_status.assert_called_once()

    # Auth related tests
    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.mcp_server.httpx.AsyncClient")
    async def test_get_api_auth_me(self, mock_httpx, mock_mcp, mock_context, url):
        client, response = create_mock_client({"user": {"id": 1, "name": "Test User"}})
        mock_httpx.return_value = client
        get = setup_capture(mock_mcp, "get-api-auth-me")
        start_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context)
        client.get.assert_called_once_with(f"{url}/api/auth/me")
        response.raise_for_status.assert_called_once()


class TestStartMCPServer:
    @patch("no_tang_doc_agent.mcp_server.mcp_server.FastMCP")
    def test_default_settings(self, mock_mcp):
        mock_mcp.return_value.run = Mock()
        start_mcp_server()
        assert mock_mcp.return_value.tool.call_count == 20
