@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

rem ---------- 1. 关闭 Tomcat ----------
if not defined TOMCAT_HOME if defined CATALINA_HOME set "TOMCAT_HOME=%CATALINA_HOME%"
if not defined TOMCAT_HOME (
    for /d %%D in ("E:\java\tomcat\apache-tomcat-*") do (
        if not defined TOMCAT_HOME if exist "%%~fD\bin\catalina.bat" set "TOMCAT_HOME=%%~fD"
    )
)
if not defined TOMCAT_HOME (
    for /d %%D in ("C:\apache-tomcat-*") do (
        if not defined TOMCAT_HOME if exist "%%~fD\bin\catalina.bat" set "TOMCAT_HOME=%%~fD"
    )
)

if defined TOMCAT_HOME (
    echo [关闭] Tomcat
    call "!TOMCAT_HOME!\bin\shutdown.bat" >nul 2>nul
) else (
    echo [提示] 未找到 Tomcat 目录，跳过
)

rem ---------- 2. 关闭后端（释放 8081） ----------
set "PID="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /r /c:"LISTENING" ^| findstr ":8081"') do if not defined PID set "PID=%%P"
if defined PID (
    echo [关闭] 后端进程 PID=!PID!
    taskkill /PID !PID! /F >nul 2>nul
) else (
    echo [提示] 8081 没有监听进程
)

powershell -NoProfile -Command "Start-Sleep -Seconds 3"

rem ---------- 3. 确认端口 ----------
set "LEFT=0"
for %%P in (8080 8081) do (
    netstat -ano | findstr /r /c:":%%P .*LISTENING" >nul
    if not errorlevel 1 (
        echo [警告] 端口 %%P 仍被占用
        set "LEFT=1"
    )
)

echo.
if "!LEFT!"=="0" (
    echo 已全部停止。
) else (
    echo 仍有端口被占用，可用 netstat -ano ^| findstr :8080 查具体 PID 后 taskkill。
)
pause
