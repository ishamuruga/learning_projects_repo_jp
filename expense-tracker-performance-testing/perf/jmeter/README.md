# JMeter Performance Test Scripts

This folder contains the Apache JMeter load test for the Expense Tracker API.

## Load Profile

- Start with 20 virtual users.
- Add another 20 users every 30 seconds.
- Continue until 100 concurrent users are active.
- Each request is validated with:
  - HTTP 200 response code
  - response time at or below 3 seconds

## Files

- `expense-tracker-concurrent-load.jmx` - JMeter test plan with five 20-user bursts.
- `run-load-test.cmd` - Windows command file to execute the test plan in non-GUI mode.

## Run

1. Install Apache JMeter.
2. Set the `JMETER_HOME` environment variable to the JMeter installation directory.
3. Run `run-load-test.cmd` from this folder.

The script writes results to `perf/jmeter/results/` and recreates the HTML report folder on each run.