@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"

set "SQL=%~dp0sql\springboot_library_2026.sql"
if not exist "%SQL%" set "SQL=%~dp0..\sql\springboot_library_2026.sql"
if not exist "%SQL%" (
    echo [错误] 未找到 sql\springboot_library_2026.sql
    pause
    exit /b 1
)

echo 即将导入数据库 springboot_library_2026，输入 MySQL root 密码继续：
mysql -uroot -p --default-character-set=utf8mb4 < "%SQL%"
if errorlevel 1 (
    echo [错误] 数据库导入失败，请确认 mysql 命令已加入 PATH。
) else (
    echo 数据库导入完成。
)
pause
