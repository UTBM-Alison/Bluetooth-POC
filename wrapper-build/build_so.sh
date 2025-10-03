#!/bin/bash

# Script pour créer un fichier .so minimal qui fonctionne
echo "=== Création d'un fichier .so minimal pour les tests ==="

# Vérifier si on a gcc
if ! command -v gcc &> /dev/null; then
    echo "ERREUR: gcc n'est pas installé"
    echo "Sur Ubuntu/Debian: sudo apt-get install gcc"
    echo "Sur CentOS/RHEL: sudo yum install gcc"
    exit 1
fi

# Vérifier JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
    echo "ATTENTION: JAVA_HOME n'est pas défini"
    echo "Tentative de détection automatique..."
    
    # Essayer de trouver Java
    if command -v java &> /dev/null; then
        JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
        echo "JAVA_HOME détecté: $JAVA_HOME"
    else
        echo "ERREUR: Java n'est pas trouvé"
        exit 1
    fi
fi

echo "Utilisation de JAVA_HOME: $JAVA_HOME"

# Aller dans le répertoire native/src
cd "$(dirname "$0")/native/src" || exit 1

# Compiler le fichier .so
echo "Compilation de libBLEServer.so..."

gcc -shared -fPIC \
    -I"$JAVA_HOME/include" \
    -I"$JAVA_HOME/include/linux" \
    -I../include \
    -o ../../src/main/resources/libBLEServer.so \
    BLEServer_linux.c

if [ $? -eq 0 ]; then
    echo "✅ libBLEServer.so créé avec succès !"
    echo "Fichier créé: src/main/resources/libBLEServer.so"
    
    # Vérifier le fichier
    file ../../src/main/resources/libBLEServer.so
    echo ""
    echo "Vous pouvez maintenant tester avec: mvn test"
else
    echo "❌ Erreur lors de la compilation"
    exit 1
fi