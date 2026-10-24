@echo off
REM Compiles the program and the tests, then runs all automated tests.

call compile.bat
if errorlevel 1 exit /b 1

javac -encoding UTF-8 -d out -cp out -sourcepath . test\RunAllTests.java
if errorlevel 1 (
    echo.
    echo Test compilation FAILED. Read the error messages above.
    exit /b 1
)
java -cp out test.RunAllTests
