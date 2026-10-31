@echo off
setlocal
for %%I in ("%~dp0..") do set "PROJECT_ROOT=%%~fI"
if not exist "%PROJECT_ROOT%\build\text-ui-test" mkdir "%PROJECT_ROOT%\build\text-ui-test"

:chooseWorkspace
set "TEST_WORKSPACE=%PROJECT_ROOT%\build\text-ui-test\run-%RANDOM%-%RANDOM%-%RANDOM%"
if exist "%TEST_WORKSPACE%" goto chooseWorkspace
mkdir "%TEST_WORKSPACE%"
if errorlevel 1 exit /b 1
pushd "%TEST_WORKSPACE%"
java -jar "%PROJECT_ROOT%\build\libs\bob.jar" < "%PROJECT_ROOT%\text-ui-test\input.txt" > "%PROJECT_ROOT%\build\text-ui-test\ACTUAL.txt"
set "TEST_EXIT=%ERRORLEVEL%"
popd
if not "%TEST_EXIT%" == "0" exit /b 1

fc /L /T "%PROJECT_ROOT%\text-ui-test\EXPECTED.txt" "%PROJECT_ROOT%\build\text-ui-test\ACTUAL.txt"
if errorlevel 1 exit /b 1
echo Console regression test passed.
exit /b 0
