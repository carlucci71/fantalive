FantaLive - sottoprogetto mobile (Cordova wrapper)

Questo sottoprogetto contiene i file base per il wrapper Cordova che incapsula l'app web legacy (AngularJS) presente in `../src/main/webapp`.

Scopo
- Fornire la struttura minima di un progetto Cordova (senza piattaforme) così da poter aggiungere iOS quando sarai sul Mac.
- Evitare di commitare piattaforme, plugin e dipendenze generate.

File principali
- `config.xml`  -> configurazione Cordova
- `package.json` -> metadata e comandi helper
- `www/` -> qui vengono copiati i file web (usare `../sync-mobile.bat` dalla root per popolare questa cartella)

Flusso rapido (Windows)
1) Sincronizza i file web nel wrapper:

```cmd
cd C:\Users\D.Carlucci\Documents\GitHub\fantalive
sync-mobile.bat
```

2) Se vuoi installare dipendenze locali (opzionale):

```cmd
cd mobile
npm install -g cordova    # se non hai cordova globalmente
npm install                # installa eventuali dipendenze locali
cordova --version
```

Nota: non aggiungere piattaforme su Windows. Aggiungerai iOS su Mac.

Passaggi consigliati su Mac (quando sei pronto per Xcode)
1) Clona/copia il repo sul Mac e assicurati che `mobile/www` contenga i file web (puoi usare lo script di sync o `rsync`).

2) Installa Node/Cordova sul Mac (se necessario):

```bash
brew install node
npm install -g cordova
```

3) Dalla cartella `mobile` aggiungi la piattaforma iOS e prepara il progetto:

```bash
cd mobile
cordova platform add ios
cordova prepare ios
```

4) Apri il progetto in Xcode:
- Se è presente `platforms/ios/Podfile` e CocoaPods è stato eseguito, apri `platforms/ios/*.xcworkspace`.
- Altrimenti apri `platforms/ios/*.xcodeproj`.

5) Configura il signing in Xcode (Team, provisioning profile) e fai Archive → Distribute per TestFlight/App Store.

Suggerimenti e note
- Il contenuto di `www/` verrà sovrascritto dal tuo script `sync-mobile.bat`; mantieni il sorgente principale in `src/main/webapp`.
- Per firmare e caricare in automatico valuta `fastlane` o un runner macOS in CI.

Se vuoi che io:
- aggiunga un esempio di `GitHub Actions` workflow per build iOS (macOS runner), oppure
- generi una lista di plugin Cordova più usati per funzionalità comuni (camera, file, geolocation),

dimmi e procedo a crearli nel repo.

