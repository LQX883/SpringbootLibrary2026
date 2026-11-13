@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

rem 1) 命令行参数优先
if not "%~1"=="" set "TOMCAT_HOME=%~1"

rem 2) 环境变量 CATALINA_HOME / CATALINA_BASE
if not defined TOMCAT_HOME if defined CATALINA_HOME set "TOMCAT_HOME=%CATALINA_HOME%"
if not defined TOMCAT_HOME if defined CATALINA_BASE set "TOMCAT_HOME=%CATALINA_BASE%"

rem 3) 在本机常见位置自动探测
if not defined TOMCAT_HOME if exist "E:\java\tomcat" (
    for /d %%D in ("E:\java\tomcat\apache-tomcat-*") do (
        if not defined TOMCAT_HOME if exist "%%~fD\bin\catalina.bat" set "TOMCAT_HOME=%%~fD"
    )
)
if not defined TOMCAT_HOME if exist "C:\apache-tomcat" (
    for /d %%D in ("C:\apache-tomcat\apache-tomcat-*") do (
        if not defined TOMCAT_HOME if exist "%%~fD\bin\catalina.bat" set "TOMCAT_HOME=%%~fD"
    )
)
if not defined TOMCAT_HOME (
    for /d %%D in ("C:\apache-tomcat-*") do (
        if not defined TOMCAT_HOME if exist "%%~fD\bin\catalina.bat" set "TOMCAT_HOME=%%~fD"
    )
)
if not defined TOMCAT_HOME (
    for /d %%D in ("C:\Program Files\Apache Software Foundation\Tomcat*") do (
        if not defined TOMCAT_HOME if exist "%%~fD\bin\catalina.bat" set "TOMCAT_HOME=%%~fD"
    )
)

if defined TOMCAT_HOME if not exist "%TOMCAT_HOME%\bin\catalina.bat" (
    echo [提示] "%TOMCAT_HOME%" 下没有 bin\catalina.bat，按无效路径处理。
    set "TOMCAT_HOME="
)

if not defined TOMCAT_HOME (
    echo [错误] 未能自动找到 Tomcat 安装目录
    echo.
    echo 方式一：把 Tomcat 路径作为参数传入（最可靠）
    echo   deploy-frontend.bat "E:\java\tomcat\apache-tomcat-9.0.123"
    echo.
    echo 方式二：先在当前窗口设置环境变量后再次运行
    echo   set TOMCAT_HOME=E:\java\tomcat\apache-tomcat-9.0.123
    echo   deploy-frontend.bat
    echo.
    pause
    exit /b 1
)

set "FRONTEND=%~dp0frontend"
if not exist "%FRONTEND%\index.html" set "FRONTEND=%~dp0..\frontend"
if not exist "%FRONTEND%\index.html" (
    echo [错误] 未找到前端目录 frontend
    echo        本脚本需要与 frontend 目录同级，或位于项目根目录的 scripts 子目录中。
    pause
    exit /b 1
)

set "DEST=%TOMCAT_HOME%\webapps\library2026"
echo 使用 Tomcat : %TOMCAT_HOME%
echo 前端来源    : %FRONTEND%
echo 部署目标    : %DEST%
echo.

if exist "%DEST%" (
    rmdir /s /q "%DEST%" 2>nul
    if exist "%DEST%" (
        echo [错误] 无法删除旧的部署目录，Tomcat 可能正在运行。
        echo        请先关闭 Tomcat（bin\shutdown.bat）后再试。
        pause
        exit /b 1
    )
)
mkdir "%DEST%"
xcopy "%FRONTEND%" "%DEST%" /E /I /Q /Y >nul
if errorlevel 1 (
    echo [错误] 复制前端文件失败。
    pause
    exit /b 1
)

echo ==========================================
echo  前端部署完成
echo  启动 Tomcat: %TOMCAT_HOME%\bin\startup.bat
echo  访问地址  : http://localhost:8080/library2026/
echo ==========================================
pause
