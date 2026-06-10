# Bug Tracker MCP Server

An MCP (Model Context Protocol) server over HTTP for recording and managing bug reports. Bug data is persisted in `data/bugs.json`.

## Setup

```bash
npm install
npm start
```

The server listens on `http://localhost:3000/mcp` by default. Override with `PORT=<n> npm start`.

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/mcp` | MCP JSON-RPC (initialize / call tools) |
| GET | `/mcp` | SSE stream for server-sent events |
| DELETE | `/mcp` | Close an MCP session |
| GET | `/health` | Health check |

## MCP Tools

### `report_bug`
Record a new bug.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | ✓ | Short title |
| description | string | ✓ | Full details |
| reportedBy | string | ✓ | Reporter name / system |
| environment | string | ✓ | prod / staging / dev / etc. |
| priority | enum | – | `low` `medium` `high` `critical` (default: medium) |
| logs | string | – | Stack trace or log snippet |
| tags | string[] | – | Optional labels |

### `get_bug`
Retrieve one bug by UUID.

### `list_bugs`
List bugs with optional filters: `status`, `priority`, `reportedBy`, `environment`, `limit` (max 100).

### `update_bug`
Update any field on an existing bug (identified by `id`). Automatically updates `updatedAt`.

### `delete_bug`
Permanently remove a bug by `id`.

## Bug Schema

```json
{
  "id": "<uuid>",
  "name": "Login button unresponsive",
  "description": "Clicking login does nothing on Safari 17",
  "reportedBy": "qa-automation",
  "environment": "production",
  "priority": "high",
  "status": "open",
  "logs": "TypeError: Cannot read properties of undefined...",
  "tags": ["auth", "safari"],
  "timestamp": "2026-06-07T10:00:00.000Z",
  "updatedAt": "2026-06-07T10:05:00.000Z"
}
```

### Status values
`open` → `in_progress` → `resolved` → `closed`

## Connecting a Claude Desktop client

Add this to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "bug-tracker": {
      "url": "http://localhost:3000/mcp"
    }
  }
}
```
