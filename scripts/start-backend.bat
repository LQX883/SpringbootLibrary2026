@echo off
chcp 65001 >nul
cd /d "%~dp0"

if not exist SpringbootLibrary2026.jar (
    echo [错误] 当前目录没有 SpringbootLibrary2026.jar
    echo 请将本脚本与 Jar、lib 目录放在同一目录后重试。
    pause
    exit /b 1
)
if not exist lib (
    echo [错误] 当前目录没有 lib 依赖目录
    pause
    exit /b 1
)

echo ==========================================
echo  SpringbootLibrary2026 后端启动
echo  API: http://localhost:8081
echo ==========================================
java -jar SpringbootLibrary2026.jar
pause
