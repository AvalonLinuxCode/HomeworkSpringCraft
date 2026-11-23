@echo off
chcp 65001 >nul
setlocal
title EduPlatform 一键启动

REM ==================== 配置区（按需修改） ====================
REM 项目根目录自动取本脚本所在目录，任何机器放置均可，无需手改。
REM 如需覆盖，可在下一行手动 set "PROJECT_ROOT=你的绝对路径"。
set "PROJECT_ROOT=%~dp0"
REM 去掉 %~dp0 末尾的反斜杠，保证拼出的子目录路径规范
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
set "BACKEND_DIR=%PROJECT_ROOT%\edu-platform"
set "FRONTEND_DIR=%PROJECT_ROOT%\edu-platform-frontend"
set "BACKEND_PORT=8080"
set "FRONTEND_PORT=5173"
set "WAIT_MAX_SEC=120"
echo ============================================
echo   EduPlatform 一键启动
echo ============================================
echo.

REM ==================== 目录检查 ====================
if not exist "%BACKEND_DIR%\pom.xml" (
    echo [错误] 后端目录不存在或缺少 pom.xml：
    echo        %BACKEND_DIR%
    goto :fail
)
if not exist "%FRONTEND_DIR%\package.json" (
    echo [错误] 前端目录不存在或缺少 package.json：
    echo        %FRONTEND_DIR%
    goto :fail
)

REM ==================== 命令检查 ====================
where mvn >nul 2>&1
if errorlevel 1 (
    echo [错误] 找不到 mvn，请先配置 Maven 环境变量（PATH 含 Maven\bin）
    goto :fail
)
where node >nul 2>&1
if errorlevel 1 (
    echo [错误] 找不到 node，请先安装 Node.js 并加入 PATH
    goto :fail
)
where npm >nul 2>&1
if errorlevel 1 (
    echo [错误] 找不到 npm
    goto :fail
)

REM ==================== 端口占用检查 ====================
netstat -ano | findstr ":%BACKEND_PORT% " | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [警告] 端口 %BACKEND_PORT% 已被占用，后端可能启动失败。
    echo         查占用：netstat -ano ^| findstr :%BACKEND_PORT%
    echo.
)
netstat -ano | findstr ":%FRONTEND_PORT% " | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [警告] 端口 %FRONTEND_PORT% 已被占用，前端可能启动失败。
    echo.
)

REM ==================== 启动后端 ====================
echo [1/2] 启动后端（新窗口）...
start "EduPlatform Backend [%BACKEND_PORT%]" /D "%BACKEND_DIR%" cmd /k "mvn spring-boot:run"
echo       后端窗口标题：EduPlatform Backend [%BACKEND_PORT%]
echo.

REM ==================== 等待后端端口就绪 ====================
echo [2/2] 等待后端就绪（最多 %WAIT_MAX_SEC% 秒）...
set /a waited=0
:wait_backend
netstat -ano | findstr ":%BACKEND_PORT% " | findstr "LISTENING" >nul
if not errorlevel 1 goto backend_ready
set /a waited+=2
if %waited% geq %WAIT_MAX_SEC% (
    echo [警告] 等待超时，后端仍未监听 %BACKEND_PORT%，继续启动前端...
    goto backend_ready
)
timeout /t 2 /nobreak >nul
goto wait_backend

:backend_ready
echo       后端已就绪（等待 %waited% 秒）。
echo.

REM ==================== 启动前端 ====================
echo [2/2] 启动前端（新窗口）...
start "EduPlatform Frontend [%FRONTEND_PORT%]" /D "%FRONTEND_DIR%" cmd /k "npm run dev"
echo.

echo ============================================
echo   启动完成！
echo   后端：http://localhost:%BACKEND_PORT%
echo   前端：http://localhost:%FRONTEND_PORT%/
echo   文档：http://localhost:%BACKEND_PORT%/swagger-ui/index.html
echo ============================================
echo 停止服务：在各窗口按 Ctrl+C，或直接关闭窗口
echo.
timeout /t 5 >nul
exit /b 0

:fail
echo.
echo 启动中止，请根据上面的提示修复后重试。
pause
exit /b 1