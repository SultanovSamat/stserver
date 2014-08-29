title stserver
@echo off
set "rootdir=%cd%"
::echo %rootdir%

set "logdir=%rootdir%\log"
::echo %logdir%

set LIBS=lib
set PUB_LIB=

set cp=.
for /f "delims=" %%c   in ('dir  /b/a-d/s  %LIBS%\*.jar')  do (
    CALL setcp.bat %%c
)

set PUB_LIB=%cp%
::echo %PUB_LIB%

set MAIN_CLASS=com.jadic.STServer

::project class path
set "CLASSPATH=%CLASSPATH%;%rootdir%\resources;%PUB_LIB%"
::echo %CLASSPATH%
java -server -Dlog.dir=%logdir% %MAIN_CLASS% 