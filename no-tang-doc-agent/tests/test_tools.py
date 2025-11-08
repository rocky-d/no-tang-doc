from unittest.mock import patch

import pytest

from no_tang_doc_agent.mcp_server import FastMCPSettings, launch_mcp_server

from .helpers import create_mock_client, make_api_client_factory, setup_capture


class TestMCPTools:
    @pytest.fixture
    def url(self):
        return "http://test.example.com"

    # Team related tests
    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_get_team_by_id(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"id": 1, "name": "Test Team"})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        get = setup_capture(mock_mcp, "get-team-by-id")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await get()(mock_context, team_id=123)
        client.get.assert_called_once_with(f"{url}/api/v1/teams/123")
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_update_team_by_id(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"id": 1, "name": "Updated Team"})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        update = setup_capture(mock_mcp, "update-team-by-id")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await update()(
            mock_context, team_id=123, name="Updated Team", description="New desc"
        )
        client.put.assert_called_once_with(
            f"{url}/api/v1/teams/123",
            json={"name": "Updated Team", "description": "New desc"},
        )
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_delete_team_by_id(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"success": True})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        delete = setup_capture(mock_mcp, "delete-team-by-id")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await delete()(mock_context, team_id=123)
        client.delete.assert_called_once_with(f"{url}/api/v1/teams/123")
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_get_teams(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"teams": []})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        get = setup_capture(mock_mcp, "get-teams")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await get()(mock_context, active_only=True)
        client.get.assert_called_once_with(
            f"{url}/api/v1/teams", params={"activeOnly": True}
        )
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_get_teams_no_params(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"teams": []})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        get = setup_capture(mock_mcp, "get-teams")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await get()(mock_context)
        client.get.assert_called_once_with(f"{url}/api/v1/teams", params={})
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_create_team(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"id": 999, "name": "New Team"})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        create = setup_capture(mock_mcp, "create-team")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await create()(
            mock_context, name="New Team", description="Team description"
        )
        client.post.assert_called_once_with(
            f"{url}/api/v1/teams",
            json={"name": "New Team", "description": "Team description"},
        )
        resp.raise_for_status.assert_called_once()

    # Team member related tests
    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_update_team_member_role(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"success": True})
        factory = make_api_client_factory(client)
        mock_httpx.side_effect = factory
        mock_tools_api_client.side_effect = factory
        update = setup_capture(mock_mcp, "update-team-member-role")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await update()(mock_context, team_id=123, member_id=456, role="admin")
        client.put.assert_called_once_with(
            f"{url}/api/v1/teams/123/members/456", json={"role": "admin"}
        )
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_remove_team_member(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"success": True})
        factory = make_api_client_factory(client)
        mock_httpx.side_effect = factory
        mock_tools_api_client.side_effect = factory
        remove = setup_capture(mock_mcp, "remove-team-member")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await remove()(mock_context, team_id=123, member_id=456)
        client.delete.assert_called_once_with(f"{url}/api/v1/teams/123/members/456")
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_get_team_members(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"members": []})
        factory = make_api_client_factory(client)
        mock_httpx.side_effect = factory
        mock_tools_api_client.side_effect = factory
        get = setup_capture(mock_mcp, "get-team-members")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context, team_id=123, active_only=False)
        client.get.assert_called_once_with(
            f"{url}/api/v1/teams/123/members", params={"activeOnly": False}
        )
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_get_team_members_no_params(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"members": []})
        factory = make_api_client_factory(client)
        mock_httpx.side_effect = factory
        mock_tools_api_client.side_effect = factory
        get = setup_capture(mock_mcp, "get-team-members")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context, team_id=123)
        client.get.assert_called_once_with(f"{url}/api/v1/teams/123/members", params={})
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_add_team_member(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"success": True})
        factory = make_api_client_factory(client)
        mock_httpx.side_effect = factory
        mock_tools_api_client.side_effect = factory
        add = setup_capture(mock_mcp, "add-team-member")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await add()(mock_context, team_id=123, user_kc_id=789, role="member")
        client.post.assert_called_once_with(
            f"{url}/api/v1/teams/123/members", json={"userKcId": 789, "role": "member"}
        )
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_leave_team(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"success": True})
        factory = make_api_client_factory(client)
        mock_httpx.side_effect = factory
        mock_tools_api_client.side_effect = factory
        leave = setup_capture(mock_mcp, "leave-team")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await leave()(mock_context, team_id=123)
        client.post.assert_called_once_with(f"{url}/api/v1/teams/123/members/leave")
        resp.raise_for_status.assert_called_once()

    # Document related tests
    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_upload_document(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"id": 999, "status": "UPLOADING"})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        upload = setup_capture(mock_mcp, "upload-document")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
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
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_upload_document_no_optional_params(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"id": 999})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        upload = setup_capture(mock_mcp, "upload-document")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await upload()(mock_context, file_content="test content")
        client.post.assert_called_once_with(
            f"{url}/api/v1/documents/upload", params={}, files={"file": b"test content"}
        )
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_get_documents(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"documents": []})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        get = setup_capture(mock_mcp, "get-documents")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context, status="ACTIVE")
        client.get.assert_called_once_with(
            f"{url}/api/v1/documents", params={"status": "ACTIVE"}
        )
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_get_documents_no_status(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"documents": []})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        get = setup_capture(mock_mcp, "get-documents")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context)
        client.get.assert_called_once_with(f"{url}/api/v1/documents", params={})
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_share_document(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"shareUrl": "http://share.link"})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        share = setup_capture(mock_mcp, "share-document")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await share()(mock_context, document_id=456, expiration_minutes=60)
        client.get.assert_called_once_with(
            f"{url}/api/v1/documents/share",
            params={"documentId": 456, "expirationMinutes": 60},
        )
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_share_document_no_expiration(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"shareUrl": "http://share.link"})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        share = setup_capture(mock_mcp, "share-document")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await share()(mock_context, document_id=456)
        client.get.assert_called_once_with(
            f"{url}/api/v1/documents/share", params={"documentId": 456}
        )
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_download_document_metadata(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"id": 456, "name": "doc.txt"})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        download = setup_capture(mock_mcp, "download-document-metadata")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await download()(mock_context, document_id=456)
        client.get.assert_called_once_with(f"{url}/api/v1/documents/download/456")
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_download_document_content(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        metadata_client, metadata_response = create_mock_client(
            {"data": {"downloadUrl": "http://cdn.example.com/file.txt"}}
        )
        content_client, content_response = create_mock_client(
            content=b"file content here"
        )
        # Create a single API client proxy whose .get returns metadata then content
        client, _ = create_mock_client()
        client.get.side_effect = [metadata_response, content_response]
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        download = setup_capture(mock_mcp, "download-document-content")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        result = await download()(mock_context, document_id=456)
        # both calls are proxied through the same mocked client
        assert client.get.call_args_list == [
            ((f"{url}/api/v1/documents/download/456",), {}),
            (("http://cdn.example.com/file.txt",), {}),
        ]
        assert result == b"file content here"

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_download_document_content_no_download_url(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        # Simulate core API returning no downloadUrl in the metadata response
        client, metadata_response = create_mock_client(json_data={})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        download = setup_capture(mock_mcp, "download-document-content")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())

        with pytest.raises(ValueError):
            await download()(mock_context, document_id=456)

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_delete_document(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"success": True})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        delete = setup_capture(mock_mcp, "delete-document")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await delete()(mock_context, document_id=456)
        client.delete.assert_called_once_with(f"{url}/api/v1/documents/456")
        resp.raise_for_status.assert_called_once()

    # Log related tests
    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_get_logs_list(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"logs": []})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        get = setup_capture(mock_mcp, "get-logs-list")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context)
        client.get.assert_called_once_with(f"{url}/api/v1/logs/list")
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_get_logs_documents(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"logs": []})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        get = setup_capture(mock_mcp, "get-logs-documents")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context, document_id=789)
        client.get.assert_called_once_with(
            f"{url}/api/v1/logs/documents", params={"documentId": 789}
        )
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_get_logs_count(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"count": 42})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        get = setup_capture(mock_mcp, "get-logs-count")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context, period="week")
        client.post.assert_called_once_with(
            f"{url}/api/v1/logs/count", params={"period": "week"}
        )
        resp.raise_for_status.assert_called_once()

    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_get_logs_count_no_period(
        self, mock_tools_api_client, mock_httpx, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"count": 100})
        mock_httpx.side_effect = make_api_client_factory(client)
        mock_tools_api_client.side_effect = make_api_client_factory(client)
        get = setup_capture(mock_mcp, "get-logs-count")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context)
        client.post.assert_called_once_with(f"{url}/api/v1/logs/count", params={})
        resp.raise_for_status.assert_called_once()

    # Auth related tests
    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    @patch("no_tang_doc_agent.mcp_server.server.APIClient")
    @patch("no_tang_doc_agent.mcp_server.tools.APIClient")
    async def test_get_api_auth_me(
        self, mock_tools_api_client, mock_server_api_client, mock_mcp, mock_context, url
    ):
        client, resp = create_mock_client({"user": {"id": 1, "name": "Test User"}})
        factory = make_api_client_factory(client)
        mock_tools_api_client.side_effect = factory
        mock_server_api_client.side_effect = factory
        get = setup_capture(mock_mcp, "get-api-auth-me")
        launch_mcp_server(base_url=url, mcp_settings=FastMCPSettings())
        await get()(mock_context)
        client.get.assert_called_once_with(f"{url}/api/auth/me")
        resp.raise_for_status.assert_called_once()
