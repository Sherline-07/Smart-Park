@echo off
echo ========================================================
echo   Compiling Smart Park Management System (Java + JDBC)
echo ========================================================

if not exist bin mkdir bin

javac -encoding UTF-8 -cp "lib\*" -d bin src\com\smartpark\model\*.java src\com\smartpark\util\*.java src\com\smartpark\dao\*.java src\com\smartpark\service\*.java src\com\smartpark\controller\*.java src\com\smartpark\server\*.java src\com\smartpark\Main.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation finished with 0 errors!
) else (
    echo [ERROR] Compilation failed. Check Java 17+ / Java 21 is installed.
)
pause
