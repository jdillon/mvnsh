@rem .
@rem Copyright (c) 2009-2010 Sonatype, Inc. All rights reserved.
@rem .
@rem This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
@rem which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
@rem .

@if "%SHELL_DEBUG%" == "" @echo off

if "%OS%"=="Windows_NT" setlocal enableextensions

:begin

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

set BOOTSCRIPT="%HOME%\.m2\mvnsh.boot.bat"
if exist "%BOOTSCRIPT%" call "%BOOTSCRIPT%"

:check_JAVACMD
if not "%JAVACMD%" == "" goto check_SHELL_HOME

:check_JAVA_HOME
if not "%JAVA_HOME%" == "" goto have_JAVA_HOME
set JAVACMD=java
goto check_SHELL_HOME

:have_JAVA_HOME
set JAVACMD=%JAVA_HOME%\bin\java
goto check_SHELL_HOME

:check_SHELL_HOME
if "%SHELL_HOME%" == "" set SHELL_HOME=%DIRNAME%..

:init
@REM Get command-line arguments, handling Windowz variants
if not "%OS%" == "Windows_NT" goto win9xME_args
if "%eval[2+2]" == "4" goto 4NT_args

@REM Regular WinNT shell
set ARGS=%*
goto execute

:win9xME_args
@REM Slurp the command line arguments.  This loop allows for an unlimited number
set ARGS=

:win9xME_args_slurp
if "x%1" == "x" goto execute
set ARGS=%ARGS% %1
shift
goto win9xME_args_slurp

:4NT_args
@REM Get arguments from the 4NT Shell from JP Software
set ARGS=%$

:execute

set BOOTJAR=%SHELL_HOME%\lib\bootstrap.jar

set COMMAND=

@REM Start the JVM
"%JAVACMD%" %JAVA_OPTS% -jar "%BOOTJAR%" %COMMAND% %ARGS%

:end

if "%OS%"=="Windows_NT" endlocal
if "%SHELL_BATCH_PAUSE%" == "on" pause
if "%SHELL_TERMINATE_CMD%" == "on" exit %ERROR_CODE%

cmd /C exit /B %ERRORLEVEL%
