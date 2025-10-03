# Code Natif BLE Server

Ce dossier contient le code natif pour le serveur BLE multi-plateforme.

## Structure

```
native/
├── include/          # Headers JNI générés
├── src/             # Code source C/C++
│   ├── BLEServer_linux.c   # Implémentation Linux
│   └── Makefile           # Build pour Linux
└── README.md        # Ce fichier
```

## Compilation

### Linux

1. **Installer les dépendances :**
   ```bash
   cd native/src
   make install-deps
   ```

2. **Générer les headers JNI :**
   ```bash
   # Depuis la racine du projet
   ./generate_jni.sh
   ```

3. **Compiler la bibliothèque :**
   ```bash
   cd native/src
   make
   ```

### Windows

La DLL Windows (`BLEServer.dll`) doit être reconstruite avec les mêmes signatures de fonctions.

## Fonctions à implémenter

Les fonctions JNI suivantes doivent être implémentées :

- `Java_main_java_BLEServer_nativeStartServer`
- `Java_main_java_BLEServer_nativeStopServer` 
- `Java_main_java_BLEServer_nativeNotify`

## Dépendances Linux

- **BlueZ** : Stack Bluetooth pour Linux
- **libbluetooth-dev** : Headers de développement
- **JNI** : Java Native Interface

## Test

Après compilation, testez avec :
```bash
mvn test
```

## Notes

- Le code actuel est un template qui doit être adapté à votre API BLE spécifique
- Pour Windows, vous devrez recréer la DLL avec les mêmes fonctions
- Les tests utilisent des mocks, donc ils fonctionneront même sans bibliothèque native réelle