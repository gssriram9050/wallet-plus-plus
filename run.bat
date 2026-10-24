@echo off
REM Starts Wallet++. Compiles first if the program has not been compiled yet.

if not exist out\main\WalletPlusPlusApp.class (
    echo Program is not compiled yet. Running compile.bat first...
    call compile.bat
    if errorlevel 1 exit /b 1
)
java -cp out main.WalletPlusPlusApp
