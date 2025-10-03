# Makefile pour compiler la bibliothèque native BLE Server Linux

# Variables
JAVA_HOME ?= /usr/lib/jvm/default-java
JNI_INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
CC = gcc
CFLAGS = -fPIC -shared -Wall -Wextra -O2
TARGET = ../src/main/resources/libBLEServer.so
SOURCE = BLEServer_linux.c

# Règles
all: $(TARGET)

$(TARGET): $(SOURCE)
	@echo "Compilation de la bibliothèque native Linux..."
	$(CC) $(CFLAGS) $(JNI_INCLUDES) -o $(TARGET) $(SOURCE)
	@echo "Bibliothèque compilée: $(TARGET)"

clean:
	rm -f $(TARGET)

install-deps:
	@echo "Installation des dépendances BlueZ (Ubuntu/Debian)..."
	sudo apt-get update
	sudo apt-get install -y libbluetooth-dev bluez-tools

help:
	@echo "Makefile pour BLEServer Linux"
	@echo ""
	@echo "Cibles disponibles:"
	@echo "  all          - Compiler la bibliothèque"
	@echo "  clean        - Nettoyer les fichiers générés"
	@echo "  install-deps - Installer les dépendances BlueZ"
	@echo "  help         - Afficher cette aide"
	@echo ""
	@echo "Variables:"
	@echo "  JAVA_HOME    - Chemin vers le JDK (actuellement: $(JAVA_HOME))"

.PHONY: all clean install-deps help