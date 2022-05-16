@echo off

REM ----------------------------------------------------------------------------
REM  program : arex bat
REM     date : 2022-3-16
REM  version : 0.1
REM ----------------------------------------------------------------------------

set CLASS_PATH="arex-cli-parent/arex-cli/target/"

if not exist "../%CLASS_PATH%arex-cli.jar" (
    echo.
    echo Can not find arex-cli.jar under %CLASS_PATH%, you can run "mvn clean install" to generate jar.
    pause
    goto :eof
)

java -cp ../%CLASS_PATH%* io.arex.cli.ArexCli

pause
