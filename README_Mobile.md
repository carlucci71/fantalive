Guida rapida: wrapper Cordova per FantaLive (AngularJS)

Scopo
- Fornire un wrapper Cordova minimale che utilizza la cartella web esistente `src/main/webapp` come `www` mobile.
- Non viene aggiunta la piattaforma Android: hai detto che Android non ti interessa. Quando sarai su Mac puoi aggiungere iOS e aprire il progetto in Xcode.

File creati
- `mobile/config.xml`  -> configurazione Cordova base
- `mobile/package.json` -> metadata e script helper
- `sync-mobile.bat` -> script Windows per copiare i file web in `mobile/www`

Flusso di lavoro (Windows)
1) Sincronizza i file web nel wrapper:

```cmd
cd C:\Users\D.Carlucci\Documents\GitHub\fantalive
sync-mobile.bat
```

Questo copierà tutto il contenuto di `src\main\webapp` in `mobile\www` (mirroring).

2) Se vuoi inizializzare Cordova localmente (opzionale):

```cmd
cd mobile
npm install -g cordova   (se non hai cordova globalmente)
npm install
cordova --version
```

Nota: per ora non aggiungiamo piattaforme su Windows (non necessario).

Passaggi da eseguire sul Mac (quando sei pronto per Xcode / build iOS)
1) Copia il repository sul Mac (o accedi via git clone).
2) Esegui lo script di sincronizzazione (se lavori ancora su Windows esegui prima lì, altrimenti copia i file web sul Mac):

```bash
# su mac, dalla root del repo
./sync-mobile.bat   # se il file è eseguibile; in alternativa fai la copia con rsync
# oppure
rsync -av ./src/main/webapp/ ./mobile/www/
```

3) Installa Cordova (se non presente) e dipendenze:

```bash
brew install node   # se necessario
npm install -g cordova
cd mobile
npm install
```

4) Aggiungi la piattaforma iOS e prepara il progetto:

```bash
cordova platform add ios
cordova prepare ios
```

Questo creerà la cartella `mobile/platforms/ios` che contiene il progetto Xcode.

5) Apri il progetto in Xcode e configura il signing (Team, provisioning profile):

- Apri `mobile/platforms/ios/*.xcworkspace` o `*.xcodeproj` in Xcode
- Seleziona il target, imposta il Team (Apple Developer Account) e risolvi il provisioning
- Product → Archive → Distribute to App Store

Suggerimenti
- Per progetti legacy AngularJS, Cordova è spesso più semplice perché ha più plugin legacy compatibili; questo setup evita di dover riscrivere o migrare ora.
- Quando sei sul Mac e vuoi automatizzare signing e upload, considera `fastlane` o i servizi cloud (Ionic Appflow / GitHub Actions su macos runner).

Se vuoi che io:
- inizializzi un progetto Cordova più completo dentro `mobile` (es. eseguire `cordova create` e aggiungere file generati) posso farlo creando i file necessari nel repo; però il comando `cordova create` modifica metadata locali che è preferibile eseguire sul tuo ambiente (facilmente replicabile).

Dimmi se vuoi che proceda a: 1) inizializzare un progetto Cordova più completo in `mobile` (senza piattaforme), 2) creare un esempio di GitHub Actions workflow per build iOS, o 3) lasciare così e passo successivo ti spiego i comandi per Xcode.
