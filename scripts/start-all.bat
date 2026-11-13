@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
cd /d "%~dp0.."

set "MVN_JAR=dist\SpringbootLibrary2026\SpringbootLibrary2026.jar"
if not exist "%MVN_JAR%" set "MVN_JAR=target\SpringbootLibrary2026.jar"

rem ---------- 1. 检查 MySQL ----------
sc query MySQL84 >nul 2>nul
if errorlevel 1 (
    echo [提示] 未检测到 MySQL84 服务，请确认数据库已在运行。
) else (
    for /f "tokens=4" %%S in ('sc query MySQL84 ^| findstr /i "STATE"') do (
        if /i not "%%S"=="RUNNING" (
            echo [警告] MySQL84 当前不是 RUNNING 状态，先启动数据库：
            echo        net start MySQL84
        )
    )
)

rem ---------- 2. 启动后端 ----------
set "BACKEND_UP=0"
for /f %%A in ('powershell -NoProfile -Command "if (Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue) { '1' } else { '0' }"') do set "BACKEND_UP=%%A"

if "%BACKEND_UP%"=="1" (
    echo [跳过] 后端已在 8081 运行
) else (
    if not exist "%MVN_JAR%" (
        echo [错误] 未找到后端 Jar：%MVN_JAR%
        echo        请先执行 scripts\package.bat 打包。
        pause
        exit /b 1
    )
    echo [启动] 后端 %MVN_JAR%
    start "library-backend" java -jar "%MVN_JAR%"
)

rem ---------- 3. 前端部署（仅首次或缺失时） ----------
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
if not defined TOMCAT_HOME (
    echo [错误] 未找到 Tomcat，请设置 CATALINA_HOME 后重试。
    pause
    exit /b 1
)

set "WEBAPP=%TOMCAT_HOME%\webapps\library2026"
if exist "%WEBAPP%\index.html" (
    echo [跳过] 前端已部署到 %WEBAPP%
) else (
    echo [部署] 复制 frontend 到 %WEBAPP%
    call scripts\deploy-frontend.bat "!TOMCAT_HOME!" < nul
)

rem ---------- 4. 启动 Tomcat ----------
set "TOMCAT_UP=0"
for /f %%A in ('powershell -NoProfile -Command "if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) { '1' } else { '0' }"') do set "TOMCAT_UP=%%A"

if "%TOMCAT_UP%"=="1" (
    echo [跳过] Tomcat 已在 8080 运行
) else (
    echo [启动] Tomcat %TOMCAT_HOME%
    start "" "%TOMCAT_HOME%\bin\startup.bat"
)

rem ---------- 5. 等待并自检 ----------
echo.
echo 正在等待服务就绪（约 15 秒）...
powershell -NoProfile -Command "Start-Sleep -Seconds 15"

set "OK=1"
powershell -NoProfile -Command "try { $r = Invoke-WebRequest 'http://localhost:8081/api/auth/health' -UseBasicParsing -TimeoutSec 8; if ($r.StatusCode -eq 200) { 'UP' } } catch { }" | findstr /i "UP" >nul
if errorlevel 1 ( echo [失败] 后端 8081 无响应 & set "OK=0" ) else ( echo [通过] 后端 8081 正常 )
powershell -NoProfile -Command "try { $r = Invoke-WebRequest 'http://localhost:8080/library2026/' -UseBasicParsing -TimeoutSec 8; if ($r.StatusCode -eq 200) { 'UP' } } catch { }" | findstr /i "UP" >nul
if errorlevel 1 ( echo [失败] 前端 8080 无响应，请查看 %TOMCAT_HOME%\logs & set "OK=0" ) else ( echo [通过] 前端 8080 正常 )

echo.
echo ==========================================
if "%OK%"=="1" (
    echo  启动完成，浏览器访问：
    echo  http://localhost:8080/library2026/
    echo  账号 admin / admin123
) else (
    echo  启动过程中有异常，请查看上面的失败项。
)
echo ==========================================
pause
