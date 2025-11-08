from unittest.mock import AsyncMock, Mock


def create_mock_client(json_data=None, text=None, content=None):
    """Return a mocked httpx.AsyncClient-like object and a resp mock.

    The returned client implements async get/post/put/delete and async
    context manager methods so tests can await and assert calls.
    """
    client = AsyncMock()
    resp = Mock()
    if json_data is not None:
        resp.json = Mock(return_value=json_data)
    if text is not None:
        resp.text = text
    if content is not None:
        resp.content = content
    resp.raise_for_status = Mock()
    client.get = AsyncMock(return_value=resp)
    client.post = AsyncMock(return_value=resp)
    client.put = AsyncMock(return_value=resp)
    client.delete = AsyncMock(return_value=resp)
    client.__aenter__ = AsyncMock(return_value=client)
    client.__aexit__ = AsyncMock(return_value=None)
    return client, resp


def make_api_client_factory(client):
    """Return a factory to use as side_effect for patched APIClient.

    The factory captures a base_url when first called with kwargs and
    returns a Proxy object that forwards calls to the provided mocked
    client. The Proxy strips any test-only ``ctx`` kwarg before
    delegating and resolves relative URLs (starting with '/') against
    the captured base_url.
    """
    if not hasattr(make_api_client_factory, "_cache"):
        make_api_client_factory._cache = {}
    key = id(client)
    if key in make_api_client_factory._cache:
        return make_api_client_factory._cache[key]

    def factory(*args, **kwargs):
        provided_base = kwargs.get("base_url") or kwargs.get("base") or ""

        class Proxy:
            def __init__(self, client, base):
                self._client = client
                self._base = base or ""

            async def _resolve(self, url):
                base_now = getattr(factory, "_base", "") or ""
                if isinstance(url, str) and url.startswith("/"):
                    return f"{base_now}{url}"
                return url

            async def get(self, url, *a, **kw):
                kw = dict(kw)
                kw.pop("ctx", None)
                u = await self._resolve(url)
                return await self._client.get(u, *a, **kw)

            async def post(self, url, *a, **kw):
                kw = dict(kw)
                kw.pop("ctx", None)
                u = await self._resolve(url)
                return await self._client.post(u, *a, **kw)

            async def put(self, url, *a, **kw):
                kw = dict(kw)
                kw.pop("ctx", None)
                u = await self._resolve(url)
                return await self._client.put(u, *a, **kw)

            async def delete(self, url, *a, **kw):
                kw = dict(kw)
                kw.pop("ctx", None)
                u = await self._resolve(url)
                return await self._client.delete(u, *a, **kw)

            async def __aenter__(self):
                return self

            async def __aexit__(self, exc_type, exc, tb):
                return None

        if not hasattr(factory, "_base") or provided_base:
            factory._base = provided_base

        if not hasattr(factory, "_proxy") or factory._proxy is None:
            factory._proxy = Proxy(client, factory._base)
        return factory._proxy

    make_api_client_factory._cache[key] = factory
    return factory


def setup_capture(mock_mcp, tool_name):
    """Capture the function registered with @mcp.tool(name=tool_name)."""
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


def setup_capture_resource(mock_mcp, uri):
    """Capture the function registered with @mcp.resource(uri)."""
    captured = None

    def decorator(*args, **kwargs):
        def wrapper(fn):
            nonlocal captured
            if args and args[0] == uri:
                captured = fn
            return fn

        return wrapper

    mock_mcp.return_value.resource.side_effect = decorator
    mock_mcp.return_value.run = Mock()
    return lambda: captured


def setup_capture_prompt(mock_mcp, prompt_name):
    """Capture the function registered with @mcp.prompt(name=prompt_name)."""
    captured = None

    def decorator(*args, **kwargs):
        def wrapper(fn):
            nonlocal captured
            if kwargs.get("name") == prompt_name or (args and args[0] == prompt_name):
                captured = fn
            return fn

        return wrapper

    mock_mcp.return_value.prompt.side_effect = decorator
    mock_mcp.return_value.run = Mock()
    return lambda: captured
