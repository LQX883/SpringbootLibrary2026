@echo off
chcp 65001 >nul
setlocal

if not "%~1"=="" set "TOMCAT_HOME=%~1"
if "%TOMCAT_HOME%"=="" (
    echo [错误] 未设置 TOMCAT_HOME
    echo 用法：
    echo   deploy-frontend.bat "E:\apache-tomcat-9.0.123"
    echo 或先执行：
    echo   set TOMCAT_HOME=E:\apache-tomcat-9.0.123
    pause
    exit /b 1
)

set "FRONTEND=%~dp0frontend"
if not exist "%FRONTEND%\index.html" set "FRONTEND=%~dp0..\frontend"
if not exist "%FRONTEND%\index.html" (
    echo [错误] 未找到前端目录 frontend
    pause
    exit /b 1
)

set "DEST=%TOMCAT_HOME%\webapps\library2026"
if exist "%DEST%" rmdir /s /q "%DEST%"
mkdir "%DEST%"
xcopy "%FRONTEND%" "%DEST%" /E /I /Q /Y >nul

echo ==========================================
echo  前端部署完成
echo  访问地址: http://localhost:8080/library2026/
echo ==========================================
pause
