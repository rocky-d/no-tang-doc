from unittest.mock import AsyncMock, Mock


def create_mock_client(json_data=None, text=None, content=None):
    """Helper to create a mocked httpx.AsyncClient context manager and response."""
    client = AsyncMock()
    response = Mock()
    if json_data is not None:
        response.json = Mock(return_value=json_data)
    if text is not None:
        response.text = text
    if content is not None:
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
