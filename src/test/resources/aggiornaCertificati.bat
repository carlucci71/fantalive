@echo off
rem https://www.dazn.com/it-IT/news/serie-a/il-calendario-completo-della-serie-a-tim-2022-2023/b1n0ghgi7x0t1rj6zswxyho13
rem https://www.fanta.soccer/it/
rem https://leghe.fantacalcio.it/fanta-viva/formazioni/1?id=512843
rem https://api2-mtc.gazzetta.it/api/v1/sports/calendar?sportId=1&competitionId=21&day=8


for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"
set "fullstamp=%YYYY%-%MM%-%DD%_%HH%-%Min%-%Sec%"

cd "C:\Program Files\Eclipse Adoptium\jdk-8.0.332.9-hotspot\jre\lib\security"
keytool -import -noprompt -trustcacerts -alias "%fullstamp%"fs  -keystore cacerts -storepass changeit -file C:\Users\D.Carlucci\Downloads\_.fanta.soccer.crt
keytool -import -noprompt -trustcacerts -alias "%fullstamp%"dazn  -keystore cacerts -storepass changeit -file C:\Users\D.Carlucci\Downloads\www.dazn.com
keytool -import -noprompt -trustcacerts -alias "%fullstamp%"fg  -keystore cacerts -storepass changeit -file C:\Users\D.Carlucci\Downloads\_.fantacalcio.it
keytool -import -noprompt -trustcacerts -alias "%fullstamp%"ga  -keystore cacerts -storepass changeit -file C:\Users\D.Carlucci\Downloads\_.gazzetta.it
pause
