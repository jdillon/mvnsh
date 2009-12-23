@rem .
@rem Copyright (C) 2009 the original author or authors.
@rem .
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem .
@rem http://www.apache.org/licenses/LICENSE-2.0
@rem .
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem .

@if "%DEBUG%" == "" @echo off

if "%OS%"=="Windows_NT" setlocal enableextensions

:begin

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

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
