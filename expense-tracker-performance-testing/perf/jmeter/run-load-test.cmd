@echo off
setlocal

if "%JMETER_HOME%"=="" (
  echo JMETER_HOME is not set. Set it to your Apache JMeter installation directory.
  exit /b 1
)

set SCRIPT_DIR=%~dp0
set TEST_PLAN=%SCRIPT_DIR%expense-tracker-concurrent-load.jmx
set RESULTS_DIR=%SCRIPT_DIR%results
set REPORT_DIR=%RESULTS_DIR%\html-report

if not exist "%RESULTS_DIR%" (
  mkdir "%RESULTS_DIR%"
)

if exist "%REPORT_DIR%" (
  rmdir /s /q "%REPORT_DIR%"
)

"%JMETER_HOME%\bin\jmeter.bat" ^
  -n ^
  -t "%TEST_PLAN%" ^
  -l "%RESULTS_DIR%\results.jtl" ^
  -e ^
  -o "%REPORT_DIR%" ^
  -Jbase.host=localhost ^
  -Jbase.port=8080 ^
  -Jbase.protocol=http

endlocal