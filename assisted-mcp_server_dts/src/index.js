import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import express from "express";
import { randomUUID } from "crypto";
import { readFileSync, writeFileSync, existsSync, mkdirSync } from "fs";
import { z } from "zod";

const DATA_DIR = "./data";
const BUGS_FILE = `${DATA_DIR}/bugs.json`;

function log(tool, level, message, data) {
  const entry = {
    ts: new Date().toISOString(),
    tool,
    level,
    message,
    ...(data !== undefined && { data }),
  };
  const line = JSON.stringify(entry);
  if (level === "error") {
    console.error(line);
  } else {
    console.log(line);
  }
}

function ensureDataFile() {
  if (!existsSync(DATA_DIR)) mkdirSync(DATA_DIR, { recursive: true });
  if (!existsSync(BUGS_FILE)) writeFileSync(BUGS_FILE, JSON.stringify([], null, 2));
}

function loadBugs() {
  ensureDataFile();
  return JSON.parse(readFileSync(BUGS_FILE, "utf-8"));
}

function saveBugs(bugs) {
  writeFileSync(BUGS_FILE, JSON.stringify(bugs, null, 2));
}

function createServer() {
  const server = new McpServer({
    name: "bug-tracker",
    version: "1.0.0",
  });

  // Tool: report_bug
  server.tool(
    "report_bug",
    "Report a new bug. Returns the created bug with its assigned ID.",
    {
      name: z.string().min(1).describe("Short title of the bug"),
      description: z.string().min(1).describe("Detailed description of the bug"),
      reportedBy: z.string().min(1).describe("Name or identifier of the reporter"),
      environment: z.string().describe("Environment where the bug was found (e.g. production, staging, dev)"),
      priority: z.enum(["low", "medium", "high", "critical"]).default("medium").describe("Bug priority"),
      logs: z.string().optional().describe("Relevant log output or stack trace"),
      tags: z.array(z.string()).optional().describe("Optional tags for categorization"),
    },
    async ({ name, description, reportedBy, environment, priority, logs, tags }) => {
      log("report_bug", "info", "Tool invoked", { name, reportedBy, environment, priority, haslogs: !!logs, tagCount: (tags ?? []).length });

      const bugs = loadBugs();
      log("report_bug", "debug", "Loaded existing bugs", { existingCount: bugs.length });

      const bug = {
        id: randomUUID(),
        name,
        description,
        reportedBy,
        environment,
        priority,
        status: "open",
        logs: logs ?? null,
        tags: tags ?? [],
        timestamp: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      log("report_bug", "debug", "Bug object created", { id: bug.id });

      bugs.push(bug);
      saveBugs(bugs);
      log("report_bug", "info", "Bug saved successfully", { id: bug.id, name: bug.name, priority: bug.priority, totalBugs: bugs.length });

      return {
        content: [{ type: "text", text: JSON.stringify(bug, null, 2) }],
      };
    }
  );

  // Tool: get_bug
  server.tool(
    "get_bug",
    "Retrieve a single bug by its ID.",
    {
      id: z.string().uuid().describe("The bug UUID"),
    },
    async ({ id }) => {
      log("get_bug", "info", "Tool invoked", { id });

      const bugs = loadBugs();
      log("get_bug", "debug", "Loaded bugs from storage", { totalBugs: bugs.length });

      const bug = bugs.find((b) => b.id === id);
      if (!bug) {
        log("get_bug", "error", "Bug not found", { id });
        return {
          content: [{ type: "text", text: `Bug with ID "${id}" not found.` }],
          isError: true,
        };
      }

      log("get_bug", "info", "Bug found", { id: bug.id, name: bug.name, status: bug.status, priority: bug.priority });
      return { content: [{ type: "text", text: JSON.stringify(bug, null, 2) }] };
    }
  );

  // Tool: list_bugs
  server.tool(
    "list_bugs",
    "List bugs with optional filters.",
    {
      status: z.enum(["open", "in_progress", "resolved", "closed"]).optional().describe("Filter by status"),
      priority: z.enum(["low", "medium", "high", "critical"]).optional().describe("Filter by priority"),
      reportedBy: z.string().optional().describe("Filter by reporter"),
      environment: z.string().optional().describe("Filter by environment"),
      limit: z.number().int().min(1).max(100).default(20).describe("Maximum number of results"),
    },
    async ({ status, priority, reportedBy, environment, limit }) => {
      log("list_bugs", "info", "Tool invoked", { filters: { status, priority, reportedBy, environment }, limit });

      let bugs = loadBugs();
      log("list_bugs", "debug", "Loaded bugs from storage", { totalBugs: bugs.length });

      const beforeFilter = bugs.length;
      if (status) {
        bugs = bugs.filter((b) => b.status === status);
        log("list_bugs", "debug", "Applied status filter", { filter: status, remaining: bugs.length });
      }
      if (priority) {
        bugs = bugs.filter((b) => b.priority === priority);
        log("list_bugs", "debug", "Applied priority filter", { filter: priority, remaining: bugs.length });
      }
      if (reportedBy) {
        bugs = bugs.filter((b) => b.reportedBy.toLowerCase().includes(reportedBy.toLowerCase()));
        log("list_bugs", "debug", "Applied reportedBy filter", { filter: reportedBy, remaining: bugs.length });
      }
      if (environment) {
        bugs = bugs.filter((b) => b.environment.toLowerCase().includes(environment.toLowerCase()));
        log("list_bugs", "debug", "Applied environment filter", { filter: environment, remaining: bugs.length });
      }

      bugs = bugs.slice(0, limit);
      log("list_bugs", "info", "Returning results", { filteredFrom: beforeFilter, returnedCount: bugs.length, limit });

      const summary = bugs.map(({ id, name, status, priority, reportedBy, environment, timestamp }) => ({
        id, name, status, priority, reportedBy, environment, timestamp,
      }));
      return {
        content: [{ type: "text", text: JSON.stringify({ total: bugs.length, bugs: summary }, null, 2) }],
      };
    }
  );

  // Tool: update_bug
  server.tool(
    "update_bug",
    "Update fields on an existing bug.",
    {
      id: z.string().uuid().describe("The bug UUID to update"),
      name: z.string().optional().describe("New title"),
      description: z.string().optional().describe("New description"),
      status: z.enum(["open", "in_progress", "resolved", "closed"]).optional().describe("New status"),
      priority: z.enum(["low", "medium", "high", "critical"]).optional().describe("New priority"),
      environment: z.string().optional().describe("New environment value"),
      logs: z.string().optional().describe("Updated log content"),
      tags: z.array(z.string()).optional().describe("Replacement tag list"),
    },
    async ({ id, ...fields }) => {
      const changedFields = Object.keys(fields).filter((k) => fields[k] !== undefined);
      log("update_bug", "info", "Tool invoked", { id, fieldsToUpdate: changedFields });

      const bugs = loadBugs();
      log("update_bug", "debug", "Loaded bugs from storage", { totalBugs: bugs.length });

      const index = bugs.findIndex((b) => b.id === id);
      if (index === -1) {
        log("update_bug", "error", "Bug not found", { id });
        return {
          content: [{ type: "text", text: `Bug with ID "${id}" not found.` }],
          isError: true,
        };
      }

      const before = { status: bugs[index].status, priority: bugs[index].priority };
      const updated = { ...bugs[index], ...Object.fromEntries(Object.entries(fields).filter(([, v]) => v !== undefined)), updatedAt: new Date().toISOString() };
      bugs[index] = updated;
      saveBugs(bugs);

      log("update_bug", "info", "Bug updated successfully", {
        id,
        fieldsChanged: changedFields,
        statusChange: before.status !== updated.status ? { from: before.status, to: updated.status } : undefined,
        priorityChange: before.priority !== updated.priority ? { from: before.priority, to: updated.priority } : undefined,
      });

      return { content: [{ type: "text", text: JSON.stringify(updated, null, 2) }] };
    }
  );

  // Tool: delete_bug
  server.tool(
    "delete_bug",
    "Permanently delete a bug by ID.",
    {
      id: z.string().uuid().describe("The bug UUID to delete"),
    },
    async ({ id }) => {
      log("delete_bug", "info", "Tool invoked", { id });

      const bugs = loadBugs();
      log("delete_bug", "debug", "Loaded bugs from storage", { totalBugs: bugs.length });

      const before = bugs.length;
      const remaining = bugs.filter((b) => b.id !== id);
      if (remaining.length === before) {
        log("delete_bug", "error", "Bug not found — nothing deleted", { id });
        return {
          content: [{ type: "text", text: `Bug with ID "${id}" not found.` }],
          isError: true,
        };
      }

      saveBugs(remaining);
      log("delete_bug", "info", "Bug deleted successfully", { id, remainingBugs: remaining.length });

      return { content: [{ type: "text", text: `Bug "${id}" deleted successfully.` }] };
    }
  );

  return server;
}

// Express app
const app = express();
app.use(express.json());

const transports = new Map();

app.post("/mcp", async (req, res) => {
  const sessionId = req.headers["mcp-session-id"];

  let transport;
  if (sessionId && transports.has(sessionId)) {
    transport = transports.get(sessionId);
  } else {
    transport = new StreamableHTTPServerTransport({
      sessionIdGenerator: () => randomUUID(),
      onsessioninitialized: (id) => transports.set(id, transport),
    });
    transport.onclose = () => {
      if (transport.sessionId) transports.delete(transport.sessionId);
    };
    const server = createServer();
    await server.connect(transport);
  }

  await transport.handleRequest(req, res, req.body);
});

app.get("/mcp", async (req, res) => {
  const sessionId = req.headers["mcp-session-id"];
  if (!sessionId || !transports.has(sessionId)) {
    return res.status(400).json({ error: "Missing or unknown session ID" });
  }
  await transports.get(sessionId).handleRequest(req, res);
});

app.delete("/mcp", async (req, res) => {
  const sessionId = req.headers["mcp-session-id"];
  if (!sessionId || !transports.has(sessionId)) {
    return res.status(400).json({ error: "Missing or unknown session ID" });
  }
  await transports.get(sessionId).handleRequest(req, res);
});

app.get("/health", (_req, res) => res.json({ status: "ok", server: "bug-tracker-mcp" }));

const PORT = process.env.PORT ?? 4000;
app.listen(PORT, () => {
  console.log(`Bug Tracker MCP Server running on http://localhost:${PORT}/mcp`);
  console.log(`Health check: http://localhost:${PORT}/health`);
});
