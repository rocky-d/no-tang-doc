import datetime as dt
import sys
import traceback as tb
from types import TracebackType

from .mcp_server import *
from . import (
    mcp_server,
)

__all__ = mcp_server.__all__


def _excepthook(
    exc_type: type[BaseException],
    exc_value: BaseException,
    exc_traceback: TracebackType | None,
) -> None:
    print(dt.datetime.now(), file=sys.stderr)
    tb.print_exception(exc_type, exc_value, exc_traceback)


sys.excepthook = _excepthook
