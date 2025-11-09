import enum
import threading
from types import TracebackType
from typing import Self

import httpx
from mcp import ServerSession
from mcp.server.fastmcp import Context

__all__ = [
    "APIClient",
    "APIClientState",
    "LazySingletonMeta",
]


class LazySingletonMeta(type):
    _singletons = {}
    _lock = threading.Lock()

    def __call__(
        cls,
        *args,
        **kwargs,
    ) -> object:
        if cls not in cls._singletons:
            with cls._lock:
                if cls not in cls._singletons:
                    instance = super().__call__(*args, **kwargs)
                    cls._singletons[cls] = instance
        return cls._singletons[cls]


class APIClientState(enum.StrEnum):
    UNOPENED = "UNOPENED"
    OPENED = "OPENED"
    CLOSED = "CLOSED"


class APIClient(metaclass=LazySingletonMeta):
    """
    Singleton HTTP client.

    NOTE: Only the first instantiation's parameters are used.
    Subsequent calls with different parameters will return the
    same instance without warning.
    """

    _client: httpx.AsyncClient
    _state: APIClientState

    @property
    def state(
        self,
    ) -> APIClientState:
        return self._state

    def __init__(
        self,
        *args,
        **kwargs,
    ) -> None:
        self._client = httpx.AsyncClient(*args, **kwargs)
        self._state = APIClientState.UNOPENED

    async def request(
        self,
        method: str,
        url: httpx.URL | str,
        *,
        ctx: Context[ServerSession, None] | None = None,
        **kwargs,
    ) -> httpx.Response:
        match self._state:
            case APIClientState.UNOPENED:
                await self.aopen()
            case APIClientState.OPENED:
                pass
            case APIClientState.CLOSED:
                raise RuntimeError("Cannot make requests with a closed client.")
        if ctx is not None:
            authorization = ctx.request_context.request.headers["authorization"]
            kwargs.setdefault("headers", {})["Authorization"] = authorization
        return await self._client.request(method, url, **kwargs)

    async def get(
        self,
        url: httpx.URL | str,
        *,
        ctx: Context[ServerSession, None] | None = None,
        **kwargs,
    ) -> httpx.Response:
        return await self.request("GET", url, ctx=ctx, **kwargs)

    async def options(
        self,
        url: httpx.URL | str,
        *,
        ctx: Context[ServerSession, None] | None = None,
        **kwargs,
    ) -> httpx.Response:
        return await self.request("OPTIONS", url, ctx=ctx, **kwargs)

    async def head(
        self,
        url: httpx.URL | str,
        *,
        ctx: Context[ServerSession, None] | None = None,
        **kwargs,
    ) -> httpx.Response:
        return await self.request("HEAD", url, ctx=ctx, **kwargs)

    async def post(
        self,
        url: httpx.URL | str,
        *,
        ctx: Context[ServerSession, None] | None = None,
        **kwargs,
    ) -> httpx.Response:
        return await self.request("POST", url, ctx=ctx, **kwargs)

    async def put(
        self,
        url: httpx.URL | str,
        *,
        ctx: Context[ServerSession, None] | None = None,
        **kwargs,
    ) -> httpx.Response:
        return await self.request("PUT", url, ctx=ctx, **kwargs)

    async def patch(
        self,
        url: httpx.URL | str,
        *,
        ctx: Context[ServerSession, None] | None = None,
        **kwargs,
    ) -> httpx.Response:
        return await self.request("PATCH", url, ctx=ctx, **kwargs)

    async def delete(
        self,
        url: httpx.URL | str,
        *,
        ctx: Context[ServerSession, None] | None = None,
        **kwargs,
    ) -> httpx.Response:
        return await self.request("DELETE", url, ctx=ctx, **kwargs)

    async def aopen(
        self,
    ) -> None:
        match self._state:
            case APIClientState.UNOPENED:
                pass
            case APIClientState.OPENED:
                raise RuntimeError("Cannot reopen an opened client.")
            case APIClientState.CLOSED:
                raise RuntimeError("Cannot reopen a closed client.")
        self._state = APIClientState.OPENED
        await self._client.__aenter__()

    async def aclose(
        self,
        exc_type: type[BaseException] | None = None,
        exc_value: BaseException | None = None,
        exc_traceback: TracebackType | None = None,
    ) -> None:
        match self._state:
            case APIClientState.UNOPENED:
                raise RuntimeError("Cannot close an unopened client.")
            case APIClientState.OPENED:
                pass
            case APIClientState.CLOSED:
                raise RuntimeError("Cannot reclose a closed client.")
        await self._client.__aexit__(exc_type, exc_value, exc_traceback)
        self._state = APIClientState.CLOSED

    async def __aenter__(
        self,
    ) -> Self:
        await self.aopen()
        return self

    async def __aexit__(
        self,
        exc_type: type[BaseException] | None,
        exc_value: BaseException | None,
        exc_traceback: TracebackType | None,
    ) -> None:
        await self.aclose(exc_type, exc_value, exc_traceback)
