# Wrapper Build Files

Ce dossier contient tous les fichiers qui ont servi à créer les wrappers natifs pour le support multi-plateforme.

## Contenu

### `native/`
- **`src/`** : Code source C des wrappers natifs
  - `BLEServer_linux.c` : Implémentation Linux du serveur BLE
  - `BLEServer_windows.c` : Implémentation Windows du serveur BLE (template)
- **`include/`** : Headers JNI et définitions
  - `main_java_BLEServer.h` : Header JNI généré pour BLEServer
  - `jni_common.h` : Définitions communes JNI
- **`Makefile`** : Makefile pour compilation Linux/Unix

### `build_so.ps1`
Script PowerShell pour compilation automatique du fichier `.so` Linux via WSL.

## Utilisation

### Compilation Linux (.so)
```powershell
.\build_so.ps1
```

### Compilation Windows (.dll)
Les fichiers Windows sont déjà fournis dans `/resources/BLEServer.dll`.

## Architecture Multi-Plateforme

L'application Java détecte automatiquement l'OS et charge la bibliothèque appropriée :
- **Windows** : `BLEServer.dll`
- **Linux** : `libBLEServer.so`

Les bibliothèques compilées sont placées dans `/resources/` à la racine du projet.

## Notes Techniques

- Le wrapper Linux utilise BlueZ pour la gestion BLE
- Compilation testée sous WSL Ubuntu 24.04
- Requires gcc, default-jdk, et libbluetooth-dev pour Linux
- Les headers JNI sont automatiquement détectés via `javac -h`