#!/bin/bash

# Script pour générer les headers JNI et les fichiers de compilation Linux

echo "=== Génération des headers JNI pour BLEServer ==="

# Compiler d'abord les classes Java
echo "Compilation des classes Java..."
cd "$(dirname "$0")"
mvn compile

# Générer les headers JNI
echo "Génération des headers JNI..."
javac -h native/include -cp target/classes src/main/java/BLEServer.java

echo "Headers JNI générés dans native/include/"
echo ""
echo "Prochaines étapes :"
echo "1. Implémentez les fonctions natives dans native/src/BLEServer.c"
echo "2. Compilez avec: gcc -shared -fPIC -I\$JAVA_HOME/include -I\$JAVA_HOME/include/linux -o src/main/resources/libBLEServer.so native/src/BLEServer.c"
echo "3. Testez avec: mvn test"