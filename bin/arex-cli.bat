@echo off

REM ----------------------------------------------------------------------------
REM  program : arex bat
REM     date : 2022-3-16
REM  version : 0.1
REM ----------------------------------------------------------------------------

set CLASS_PATH="arex-client/target/"

if not exist "../%CLASS_PATH%arex-client-*-jar-with-dependencies.jar" (
    echo.
    echo Can not find dependencies jar under %CLASS_PATH%, you can run "mvn clean install" to generate jar.
    goto :eof
)

java -cp ../%CLASS_PATH%* io.arex.cli.ArexCli

pause