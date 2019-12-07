@echo off
set olddir=%CD%
cd %~dp0
set jarfile=ResourceCreator\target\ResourceCreator-1.0-jar-with-dependencies.jar

if ["%1"]==["help"] goto help
if ["%1"]==["-help"] goto help
if ["%1"]==["/help"] goto help
if ["%1"]==["?"] goto help
if ["%1"]==["/?"] goto help
if ["%1"]==["-?"] goto help
if ["%1"]==[""] goto usage
if ["%2"]==[""] goto usage

set modid=%1
set name=%2

set argshift=2

::shifts arguments left by %argshift%
for /f "usebackq tokens=%argshift%*" %%i in (`echo %*`) DO @ set params=%%j

set templates=ResourceCreator\templates
set out=..\src\main\resources\assets\storagedrawersunlimited
set options=-i -iq -noreplacelang
set textures=%out%\textures\block

java -jar "%jarfile%" -templates "%templates%" -out "%out%" -modid "%modid%" -base "%textures%\%modid%\base\base_%name%.png" -trim "%textures%\%modid%\base\trim_%name%.png" -face "%textures%\%modid%\base\face_%name%.png" -materials "%textures%\%modid%\base\materials_%name%.json" -automaterials %options% %params%
goto done

:usage
echo Invalid arguments.
echo Usage: makeresources.bat MODID NAME
echo where NAME is the name of base_NAME.png and trim_NAME.png in MODID\base\ directory.
goto done

:help
java -jar "%jarfile%" -help

:done
cd %olddir%