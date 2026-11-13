@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0.."

echo ==========================================
echo  SpringbootLibrary2026 构建脚本
echo ==========================================

set "MVN=mvn"
where mvn >nul 2>nul
if errorlevel 1 (
    if defined MAVEN_HOME if exist "%MAVEN_HOME%\bin\mvn.cmd" set "MVN=%MAVEN_HOME%\bin\mvn.cmd"
    if exist "E:\java\maven\bin\mvn.cmd" set "MVN=E:\java\maven\bin\mvn.cmd"
)

call "%MVN%" clean package -DskipTests
if errorlevel 1 (
    echo.
    echo [错误] Maven 构建失败，请检查 JAVA_HOME 与 Maven 配置。
    pause
    exit /b 1
)

if not exist target\SpringbootLibrary2026.jar (
    echo [错误] 未找到 target\SpringbootLibrary2026.jar
    pause
    exit /b 1
)
if not exist target\lib (
    echo [错误] 未找到 target\lib，依赖分离打包失败。
    pause
    exit /b 1
)

if exist dist rmdir /s /q dist
mkdir dist\SpringbootLibrary2026
mkdir dist\SpringbootLibrary2026\uploads

copy target\SpringbootLibrary2026.jar dist\SpringbootLibrary2026\ >nul
xcopy target\lib dist\SpringbootLibrary2026\lib /E /I /Q /Y >nul
xcopy frontend dist\SpringbootLibrary2026\frontend /E /I /Q /Y >nul
xcopy sql dist\SpringbootLibrary2026\sql /E /I /Q /Y >nul
copy README.md dist\SpringbootLibrary2026\ >nul
copy scripts\start-backend.bat dist\SpringbootLibrary2026\ >nul
copy scripts\deploy-frontend.bat dist\SpringbootLibrary2026\ >nul
copy scripts\start-all.bat dist\SpringbootLibrary2026\ >nul
copy scripts\stop-all.bat dist\SpringbootLibrary2026\ >nul

powershell -NoProfile -Command "Compress-Archive -Path 'dist\SpringbootLibrary2026\*' -DestinationPath 'dist\SpringbootLibrary2026-separated.zip' -Force"

echo.
echo ==========================================
echo  构建完成
echo  可执行 Jar : dist\SpringbootLibrary2026\SpringbootLibrary2026.jar
echo  依赖目录   : dist\SpringbootLibrary2026\lib
echo  前端目录   : dist\SpringbootLibrary2026\frontend
echo  压缩包     : dist\SpringbootLibrary2026-separated.zip
echo ==========================================
pause
