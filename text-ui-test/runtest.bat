@echo off
setlocal
cd /d "%~dp0"
if not exist "..\build\text-ui-test" mkdir "..\build\text-ui-test"

java -jar "..\build\libs\bob.jar" < input.txt > "..\build\text-ui-test\ACTUAL.txt"
if errorlevel 1 exit /b 1

fc /L /T EXPECTED.txt "..\build\text-ui-test\ACTUAL.txt"
if errorlevel 1 exit /b 1
echo Console regression test passed.
exit /b 0
