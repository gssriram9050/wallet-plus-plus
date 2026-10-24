@echo off
REM Compiles the Wallet++ application into the "out" folder.
REM Run this from the project root (the folder that contains "src").

if not exist out mkdir out
javac -encoding UTF-8 -d out -sourcepath src src\main\WalletPlusPlusApp.java
if errorlevel 1 (
    echo.
    echo Compilation FAILED. Read the error messages above.
    exit /b 1
)
echo Compilation successful. Class files are in the "out" folder.
