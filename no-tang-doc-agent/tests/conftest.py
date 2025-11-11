from unittest.mock import MagicMock

import pytest


@pytest.fixture
def mock_context():
    ctx = MagicMock()
    ctx.request_context.request.headers = {"authorization": "Bearer test-token"}
    return ctx
