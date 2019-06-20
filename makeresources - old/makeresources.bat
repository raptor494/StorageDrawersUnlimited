@echo off
if "%1"=="" goto usage
if "%2"=="" goto usage
set modid=%1
set name=%2
for /f "usebackq tokens=2*" %%i in (`echo %*`) DO @ set params=%%j
java -jar ..\..\..\..\..\..\..\tools\ResourceCreator\target\ResourceCreator-1.0-jar-with-dependencies.jar -templates ..\..\..\..\..\..\..\tools\ResourceCreator\templates -out ..\..\ -modid "%modid%" -base "%modid%\base\base_%name%.png" -trim "%modid%\base\trim_%name%.png" %params%
goto done

:usage
echo Invalid arguments.
echo Usage: makeresources.bat MODID NAME
echo where NAME is the name of base_NAME.png and trim_NAME.png in MODID\base\ directory.
:done
