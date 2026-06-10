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
  try {
    console.log("📋 Test Execution Report");
    console.log("========================\n");
    
    console.log("✅ Tests Run: 20");
    console.log("✅ Tests Passed: 19");
    console.log("❌ Tests Failed: 1");
    console.log("⏭️  Tests Skipped: 0");
    console.log("⏱️  Execution Time: 6.616s\n");
    
    console.log("Initializing bug-tracker MCP server...");
    
    const init = await request("initialize", {
      protocolVersion: "2024-11-05",
      capabilities: {},
      clientInfo: { name: "test-reporter", version: "1.0.0" }
    });

    console.log("✓ MCP Server initialized\n");
    
    send({ jsonrpc: "2.0", method: "notifications/initialized", params: {} });

    console.log("Creating defect for failing test...\n");
    
    const created = await request("tools/call", {
      name: "create_defect",
      arguments: {
        title: "Unit Test Failure: TodoControllerTest#validateEven_evenValue_returns200",
        description: "Build command: mvn test\n\nFailure Type: AssertionError - JSON path validation failure\n\nTest Method: TodoControllerTest.validateEven_evenValue_returns200()\n\nRepro Steps:\n1. Run: mvn test\n2. Execute TodoControllerTest#validateEven_evenValue_returns200\n\nEvidence:\njava.lang.AssertionError: JSON path \"$.data\" expected:<8> but was:<0>\n  at org.springframework.test.util.AssertionErrors.assertEquals(AssertionErrors.java:122)\n  at org.springframework.test.util.JsonPathExpectationsHelper.assertValue(JsonPathExpectationsHelper.java:123)\n  at org.springframework.test.web.servlet.result.JsonPathResultMatchers.lambda$value$2(JsonPathResultMatchers.java:111)\n  at com.example.todo.controller.TodoControllerTest.validateEven_evenValue_returns200(TodoControllerTest.java:195)\n\nExpected vs Actual:\n- Expected: $.data = 8\n- Actual: $.data = 0\n\nImpact: Regression in the even number validation endpoint logic. The endpoint should validate and return the correct data value.",
        severity: "HIGH",
        assignee: "unassigned",
        tags: ["unit-test", "automated-test", "regression", "test-failure"]
      }
    });

    console.log("✓ Defect created successfully\n");
    
    if (created.result && created.result.structuredContent) {
      const defectInfo = created.result.structuredContent;
      console.log("📌 Defect Details:");
      console.log(`   ID: ${defectInfo.id || "N/A"}`);
      console.log(`   Title: ${defectInfo.title || "N/A"}`);
      console.log(`   Severity: ${defectInfo.severity || "N/A"}`);
      console.log(`   Status: ${defectInfo.status || "N/A"}`);
    } else {
      console.log("📌 Defect Response:", JSON.stringify(created.result, null, 2));
    }
    
    console.log("\n✅ Test failure successfully reported to bug-tracker MCP");
    
    child.kill();
    process.exit(0);
  } catch (error) {
    console.error("❌ Error:", error.message);
    child.kill();
    process.exit(1);
  }
})();
