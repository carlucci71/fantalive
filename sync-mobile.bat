@echo off
REM Script di sincronizzazione: copia src\main\webapp -> mobile\www
SETLOCAL ENABLEDELAYEDEXPANSION
REM Percorsi relativi alla posizione dello script
set REPO_DIR=%~dp0
n
set SRC=%REPO_DIR%src\main\webapp
set DEST=%REPO_DIR%mobile\www

echo Sincronizzo %SRC% -> %DEST%
if not exist "%SRC%" (
  echo ERRORE: sorgente non trovata: %SRC%
  exit /b 1
)

rem Crea destinazione se non esiste
if not exist "%DEST%" (
  mkdir "%DEST%"
)

robocopy "%SRC%" "%DEST%" /MIR /FFT /XA:SH /W:1 /R:2
set RC=%ERRORLEVEL%
if %RC% GEQ 8 (
  echo Robocopy ha restituito codice errore %RC% (fallito)
  exit /b 1
)
echo Sincronizzazione completata con codice %RC%.
exit /b 0

