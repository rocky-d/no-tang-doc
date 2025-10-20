from mcp_server import mcp


def main() -> None:
    mcp.run(transport="streamable-http")


if __name__ == "__main__":
    main()
