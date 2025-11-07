from unittest.mock import Mock, patch

import pytest

from no_tang_doc_agent.mcp_server import (
    FastMCPSettings,
    JWTTokenVerifier,
    launch_mcp_server,
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


class TestLaunchMCPServer:
    @patch("no_tang_doc_agent.mcp_server.server.FastMCP")
    def test_default_settings(self, mock_mcp):
        mock_mcp.return_value.run = Mock()
        launch_mcp_server()
        assert mock_mcp.return_value.tool.call_count == 20
