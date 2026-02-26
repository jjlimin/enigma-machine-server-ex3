@echo off
REM Enigma Machine Frontend - Setup and Run Script
REM Requirements: Node.js (assumed to be installed)

echo.
echo ================================================
echo  Enigma Machine Frontend - Setup
echo ================================================
echo.

REM Check if Node.js is installed
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)

echo [1/2] Installing dependencies...
call npm install
if errorlevel 1 (
    echo ERROR: Failed to install dependencies
    pause
    exit /b 1
)

echo [2/2] Starting development server...
echo.
echo ================================================
echo  Frontend is Starting!
echo ================================================
echo.
echo Opening frontend at http://localhost:5173
echo.
echo IMPORTANT: Make sure the backend is running at:
echo http://localhost:8080
echo.
echo Press Ctrl+C to stop the server
echo.
pause

call npm run dev
