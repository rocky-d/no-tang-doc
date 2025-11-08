from unittest.mock import AsyncMock, Mock, patch

import pytest

from no_tang_doc_agent.mcp_server.api_client import (
    APIClient,
    ClientState,
    LazySingletonMeta,
)

from .helpers import create_mock_client


@pytest.fixture(autouse=True)
def clear_singleton():
    # Ensure singleton state is reset between tests
    LazySingletonMeta._singletons.clear()
    yield
    LazySingletonMeta._singletons.clear()


@patch("no_tang_doc_agent.mcp_server.api_client.httpx.AsyncClient")
async def test_open_close_and_state(mock_async_client):
    client, resp = create_mock_client()
    mock_async_client.return_value = client

    api = APIClient()
    assert api.state == ClientState.UNOPENED

    await api.open()
    assert api.state == ClientState.OPENED
    # underlying client's __aenter__ should have been awaited
    client.__aenter__.assert_awaited()

    await api.close()
    assert api.state == ClientState.CLOSED
    client.__aexit__.assert_awaited()


@patch("no_tang_doc_agent.mcp_server.api_client.httpx.AsyncClient")
async def test_request_forwards_headers_and_opens(mock_async_client):
    client, resp = create_mock_client()
    # provide a request method used by APIClient.request
    client.request = AsyncMock(return_value=resp)
    mock_async_client.return_value = client

    api = APIClient()
    ctx = Mock()
    ctx.request_context = Mock()
    ctx.request_context.request = Mock()
    ctx.request_context.request.headers = {"authorization": "Bearer test-token"}

    res = await api.get("/api/auth/me", ctx=ctx)
    # APIClient should have opened the client and forwarded Authorization header
    client.request.assert_awaited_once()
    called_args, called_kwargs = client.request.call_args
    assert called_args[0] == "GET"
    assert called_args[1] == "/api/auth/me"
    assert called_kwargs["headers"]["Authorization"] == "Bearer test-token"


@patch("no_tang_doc_agent.mcp_server.api_client.httpx.AsyncClient")
async def test_context_manager_and_errors(mock_async_client):
    client, resp = create_mock_client()
    client.request = AsyncMock(return_value=resp)
    mock_async_client.return_value = client

    # context manager opens and closes
    async with APIClient() as api:
        assert api.state == ClientState.OPENED
    assert api.state == ClientState.CLOSED

    # cannot close unopened
    LazySingletonMeta._singletons.clear()
    mock_async_client.return_value = client
    api2 = APIClient()
    with pytest.raises(RuntimeError):
        await api2.close()

    # cannot reopen an opened client
    LazySingletonMeta._singletons.clear()
    mock_async_client.return_value = client
    api3 = APIClient()
    await api3.open()
    with pytest.raises(RuntimeError):
        await api3.open()


@patch("no_tang_doc_agent.mcp_server.api_client.httpx.AsyncClient")
async def test_request_on_closed_raises(mock_async_client):
    client, resp = create_mock_client()
    client.request = AsyncMock(return_value=resp)
    mock_async_client.return_value = client

    api = APIClient()
    await api.open()
    await api.close()
    with pytest.raises(RuntimeError):
        await api.get("/some")


@patch("no_tang_doc_agent.mcp_server.api_client.httpx.AsyncClient")
async def test_singleton_behavior(mock_async_client):
    client, resp = create_mock_client()
    mock_async_client.return_value = client

    a1 = APIClient()
    a2 = APIClient()
    assert a1 is a2
    # AsyncClient constructor should have been called only once
    assert mock_async_client.call_count == 1


@patch("no_tang_doc_agent.mcp_server.api_client.httpx.AsyncClient")
async def test_all_wrappers_delegate(mock_async_client):
    client, resp = create_mock_client()
    client.request = AsyncMock(return_value=resp)
    mock_async_client.return_value = client

    api = APIClient()
    # call each wrapper; request will auto-open the client
    await api.options("/a")
    await api.head("/b")
    await api.post("/c")
    await api.put("/d")
    await api.patch("/e")
    await api.delete("/f")

    # collect method names seen
    called_methods = [call.args[0] for call in client.request.call_args_list]
    assert called_methods == ["OPTIONS", "HEAD", "POST", "PUT", "PATCH", "DELETE"]


@patch("no_tang_doc_agent.mcp_server.api_client.httpx.AsyncClient")
async def test_init_uses_first_args(mock_async_client):
    client, resp = create_mock_client()
    mock_async_client.return_value = client

    a1 = APIClient(base_url="http://first.example")
    a2 = APIClient(base_url="http://second.example")
    assert a1 is a2
    mock_async_client.assert_called_once()
    # the first instantiation's kwargs should be the ones passed to AsyncClient
    called_args, called_kwargs = mock_async_client.call_args
    assert called_kwargs.get("base_url") == "http://first.example"


@patch("no_tang_doc_agent.mcp_server.api_client.httpx.AsyncClient")
async def test_close_reclose_raises(mock_async_client):
    client, resp = create_mock_client()
    mock_async_client.return_value = client

    api = APIClient()
    await api.open()
    await api.close()
    with pytest.raises(RuntimeError):
        await api.close()


@patch("no_tang_doc_agent.mcp_server.api_client.httpx.AsyncClient")
async def test_request_without_ctx_no_headers(mock_async_client):
    client, resp = create_mock_client()
    client.request = AsyncMock(return_value=resp)
    mock_async_client.return_value = client

    api = APIClient()
    await api.get("/some/path")
    client.request.assert_awaited()
    _, called_kwargs = client.request.call_args
    # when ctx is not provided, Authorization header should not be set
    headers = called_kwargs.get("headers")
    assert not (headers and "Authorization" in headers)


@patch("no_tang_doc_agent.mcp_server.api_client.httpx.AsyncClient")
async def test_request_when_already_open_does_not_reopen(mock_async_client):
    client, resp = create_mock_client()
    client.request = AsyncMock(return_value=resp)
    client.__aenter__ = AsyncMock()
    mock_async_client.return_value = client

    api = APIClient()
    await api.open()
    # clear the enter call history so we can assert it isn't awaited again
    client.__aenter__.reset_mock()
    await api.get("/already-open")
    client.__aenter__.assert_not_awaited()
    client.request.assert_awaited_once()


@patch("no_tang_doc_agent.mcp_server.api_client.httpx.AsyncClient")
async def test_aexit_passes_exc_info_to_underlying(mock_async_client):
    client, resp = create_mock_client()
    client.__aenter__ = AsyncMock()
    client.__aexit__ = AsyncMock()
    mock_async_client.return_value = client

    api = APIClient()
    await api.open()
    await api.__aexit__(ValueError, ValueError("bad"), None)
    client.__aexit__.assert_awaited_once()
    called_args = client.__aexit__.call_args[0]
    assert called_args[0] is ValueError
    assert isinstance(called_args[1], ValueError)
    assert called_args[2] is None
    assert api.state == ClientState.CLOSED


def test_clientstate_values():
    assert ClientState.UNOPENED.value == "UNOPENED"
    assert ClientState.OPENED.value == "OPENED"
    assert ClientState.CLOSED.value == "CLOSED"


def test_lazy_singleton_meta_with_new_class():
    # directly exercise LazySingletonMeta to ensure the inner-create branch
    LazySingletonMeta._singletons.clear()

    class T(metaclass=LazySingletonMeta):
        def __init__(self):
            self.value = 1

    t1 = T()
    t2 = T()
    assert t1 is t2
    assert LazySingletonMeta._singletons.get(T) is t1


@patch("no_tang_doc_agent.mcp_server.api_client.httpx.AsyncClient")
async def test_open_after_closed_raises(mock_async_client):
    client, resp = create_mock_client()
    mock_async_client.return_value = client

    api = APIClient()
    await api.open()
    await api.close()
    with pytest.raises(RuntimeError):
        await api.open()
