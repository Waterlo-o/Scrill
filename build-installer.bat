@echo off
chcp 65001 > nul
echo ================================
echo   Building Scrill Installer
echo ================================

:: Проверяем наличие WiX
where candle >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: WiX Toolset not found!
    echo Download from: https://wixtoolset.org/releases/
    pause
    exit /b 1
)

:: 1. Собираем FAT JAR
echo [1/4] Building JAR...
call mvnw.cmd clean package -q
if %errorlevel% neq 0 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)
echo       Done!

:: 2. Чистим и создаём папки
if exist "installer" rmdir /s /q "installer"
if exist "app-image" rmdir /s /q "app-image"
mkdir installer

:: 3. Копируем tools в target (чтобы jpackage их подхватил)
echo [2/4] Copying tools...
xcopy /s /e /q "tools" "target\tools\"
echo       Done!

:: 4. Создаём установщик
echo [3/4] Running jpackage...
jpackage ^
    --type exe ^
    --name "Scrill" ^
    --app-version "1.0.0" ^
    --description "Scrill Music Player" ^
    --vendor "Scrill" ^
    --input target ^
    --main-jar Scrill.jar ^
    --main-class com.example.scrill.Launcher ^
    --dest installer ^
    --icon "src\main\resources\com\example\scrill\icons\icon.ico" ^
    --win-dir-chooser ^
    --win-menu ^
    --win-menu-group "Scrill" ^
    --win-shortcut ^
    --win-shortcut-prompt ^
    --java-options "-Dapp.dir=$APPDIR" ^
    --java-options "-Dfile.encoding=UTF-8" ^
    --java-options "--add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED"

if %errorlevel% neq 0 (
    echo ERROR: jpackage failed!
    pause
    exit /b 1
)

echo [4/4] Done!
echo.
echo Installer is ready in the 'installer' folder!
pause