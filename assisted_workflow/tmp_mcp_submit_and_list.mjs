import { spawn } from "node:child_process";

const child = spawn("node", ["C:/Users/rsubramanian/temp_del/nec-github-copilot/project05-mcp_server/server.js"], {
  stdio: ["pipe", "pipe", "pipe"]
});

let buffer = Buffer.alloc(0);
const pending = new Map();
let nextId = 1;

function send(message) {
  const body = Buffer.from(JSON.stringify(message), "utf8");
  const header = Buffer.from(`Content-Length: ${body.length}\r\n\r\n`, "utf8");
  child.stdin.write(Buffer.concat([header, body]));
}

function processFrames() {
  while (true) {
    const separator = buffer.indexOf("\r\n\r\n");
    if (separator === -1) return;

    const header = buffer.slice(0, separator).toString("utf8");
    const match = header.match(/Content-Length: (\d+)/i);
    if (!match) throw new Error("Missing Content-Length");

    const length = Number(match[1]);
    const start = separator + 4;
    if (buffer.length < start + length) return;

    const payload = buffer.slice(start, start + length).toString("utf8");
    buffer = buffer.slice(start + length);

    const message = JSON.parse(payload);
    if (Object.prototype.hasOwnProperty.call(message, "id") && pending.has(message.id)) {
      pending.get(message.id)(message);
      pending.delete(message.id);
    }
  }
}

child.stdout.on("data", (chunk) => {
  buffer = Buffer.concat([buffer, chunk]);
  processFrames();
});

child.stderr.on("data", () => {});

function request(method, params) {
  const id = nextId;
  nextId += 1;
  send({ jsonrpc: "2.0", id, method, params });
  return new Promise((resolve) => pending.set(id, resolve));
}

(async () => {
  const init = await request("initialize", {
    protocolVersion: "2024-11-05",
    capabilities: {},
    clientInfo: { name: "copilot-cli-client", version: "1.0.0" }
  });

  send({ jsonrpc: "2.0", method: "notifications/initialized", params: {} });

  const created = await request("tools/call", {
    name: "create_defect",
    arguments: {
      title: "Unit Test Failure: TodoControllerTest#validateEven_evenValue_returns200",
      description: "Build command: runTests on TodoControllerTest. Failure: JSON path \"$.data\" expected:<8> but was:<0>. Evidence: src/test/java/com/example/todo/controller/TodoControllerTest.java:195. Repro: run tests for TodoControllerTest. Expected: $.data == 8. Actual: $.data == 0.",
      severity: "HIGH",
      assignee: "unassigned",
      tags: ["unit-test", "automated-test", "regression"]
    }
  });

  const listed = await request("tools/call", {
    name: "get_defects",
    arguments: {
      query: "TodoControllerTest#validateEven_evenValue_returns200"
    }
  });

  console.log(JSON.stringify({ initialize: init.result, created: created.result, listed: listed.result }, null, 2));
  child.kill();
})().catch((error) => {
  console.error(error);
  child.kill();
  process.exit(1);
});
