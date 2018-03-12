@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  powerbroker startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and POWERBROKER_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xint" "-Xms512m" "-XX:-UseGCOverheadLimit" "-Dfile.encoding=utf-8" "-Djava.security.egd=file:///dev/urandom" "-Djdk.tls.ephemeralDHKeySize=1024" "-Dorg.slf4j.simpleLogger.defaultLogLevel=error"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\powerbroker_3.jar;%APP_HOME%\lib\commons-io-2.2.jar;%APP_HOME%\lib\commons-lang3-3.4.jar;%APP_HOME%\lib\httpclient-4.5.1.jar;%APP_HOME%\lib\mapdb-2.0-beta8.jar;%APP_HOME%\lib\commons-cli-1.3.jar;%APP_HOME%\lib\commons-fileupload-1.3.1.jar;%APP_HOME%\lib\jline-2.8.jar;%APP_HOME%\lib\netty-all-4.0.34.Final.jar;%APP_HOME%\lib\protobuf-java-3.0.0-beta-2.jar;%APP_HOME%\lib\log4j-1.2.17.jar;%APP_HOME%\lib\httpcore-4.4.3.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-codec-1.9.jar

@rem Execute powerbroker
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %POWERBROKER_OPTS%  -classpath "%CLASSPATH%" net.roboticapex.place.StacMain %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable POWERBROKER_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%POWERBROKER_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
