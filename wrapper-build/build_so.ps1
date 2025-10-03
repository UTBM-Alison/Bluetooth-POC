# Script PowerShell pour créer un fichier .so minimal sur Windows
# Utilise WSL si disponible, sinon crée un fichier stub

Write-Host "=== Création de libBLEServer.so pour Linux ===" -ForegroundColor Green

$soPath = "src\main\resources\libBLEServer.so"

# Vérifier si WSL est disponible
$wslAvailable = $false
try {
    $wslVersion = wsl --version 2>$null
    if ($LASTEXITCODE -eq 0) {
        $wslAvailable = $true
        Write-Host "✅ WSL détecté" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  WSL non disponible" -ForegroundColor Yellow
}

if ($wslAvailable) {
    Write-Host "Compilation avec WSL..." -ForegroundColor Cyan
    
    # Obtenir le chemin Windows converti en format WSL
    $windowsPath = $PWD.Path
    $wslPath = $windowsPath -replace '^([A-Z]):', '/mnt/$1'.ToLower() -replace '\\', '/'
    
    Write-Host "Chemin WSL: $wslPath" -ForegroundColor Gray
    
    # Vérifier et installer gcc si nécessaire
    Write-Host "Vérification de gcc..." -ForegroundColor Cyan
    $gccCheck = wsl bash -c "command -v gcc"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Installation de gcc..." -ForegroundColor Yellow
        wsl bash -c "sudo apt-get update && sudo apt-get install -y gcc"
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Impossible d'installer gcc" -ForegroundColor Red
            $wslAvailable = $false
        }
    }
    
    if ($wslAvailable) {
        # Compiler le fichier .so avec détection de JAVA_HOME
        $compileCommand = @"
cd '$wslPath'
echo 'Répertoire actuel:' && pwd
echo 'Détection de Java...'

# Détecter JAVA_HOME dans WSL
if [ -z "`$JAVA_HOME" ]; then
    echo 'JAVA_HOME non défini, détection automatique...'
    
    # Essayer de trouver Java dans WSL
    if command -v java >/dev/null 2>&1; then
        JAVA_PATH=`$(readlink -f `$(which java))
        JAVA_HOME=`$(dirname `$(dirname `$JAVA_PATH))
        echo "JAVA_HOME détecté: `$JAVA_HOME"
    else
        echo 'Java non trouvé, installation...'
        sudo apt-get update && sudo apt-get install -y default-jdk
        if [ `$? -eq 0 ]; then
            JAVA_PATH=`$(readlink -f `$(which java))
            JAVA_HOME=`$(dirname `$(dirname `$JAVA_PATH))
            echo "JAVA_HOME après installation: `$JAVA_HOME"
        else
            echo 'Impossible d installer Java'
            exit 1
        fi
    fi
else
    echo "JAVA_HOME existant: `$JAVA_HOME"
fi

echo 'Vérification des includes JNI...'
if [ ! -f "`$JAVA_HOME/include/jni.h" ]; then
    echo 'jni.h non trouvé, recherche alternative...'
    JNI_INCLUDE=`$(find /usr -name "jni.h" 2>/dev/null | head -1 | xargs dirname)
    if [ -n "`$JNI_INCLUDE" ]; then
        echo "jni.h trouvé dans: `$JNI_INCLUDE"
        JAVA_HOME=`$(dirname `$JNI_INCLUDE)
    else
        echo 'Installation de default-jdk...'
        sudo apt-get install -y default-jdk
    fi
fi

echo 'Compilation avec gcc...'
cd native/src
gcc -shared -fPIC \
    -I../include \
    -I"`$JAVA_HOME/include" \
    -I"`$JAVA_HOME/include/linux" \
    BLEServer_linux.c \
    -o ../../src/main/resources/libBLEServer.so

if [ `$? -eq 0 ]; then
    echo '✅ Compilation réussie !'
    echo 'Vérification du fichier:'
    file ../../src/main/resources/libBLEServer.so
    ls -la ../../src/main/resources/libBLEServer.so
    echo "Taille: `$(stat -c%s ../../src/main/resources/libBLEServer.so) bytes"
else
    echo '❌ Erreur de compilation'
    exit 1
fi
"@
        
        Write-Host "Commande de compilation:" -ForegroundColor Gray
        Write-Host $compileCommand -ForegroundColor DarkGray
        
        wsl bash -c $compileCommand
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Compilation WSL réussie !" -ForegroundColor Green
        } else {
            Write-Host "❌ Erreur lors de la compilation WSL" -ForegroundColor Red
            $wslAvailable = $false
        }
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ libBLEServer.so créé avec succès via WSL !" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur lors de la compilation WSL" -ForegroundColor Red
        $wslAvailable = $false
    }
}

if (-not $wslAvailable) {
    Write-Host "Création d'un fichier .so stub pour les tests..." -ForegroundColor Yellow
    
    # Créer un fichier binaire minimal ELF
    # Header ELF basique pour un fichier .so x86_64
    $elfHeader = @(
        0x7F, 0x45, 0x4C, 0x46,                    # ELF magic
        0x02,                                      # 64-bit
        0x01,                                      # Little endian
        0x01,                                      # ELF version
        0x00,                                      # System V ABI
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, # Padding
        0x03, 0x00,                                # Shared object
        0x3E, 0x00,                                # x86-64
        0x01, 0x00, 0x00, 0x00                     # Version
    )
    
    Write-Host "Création du fichier stub..." -ForegroundColor Cyan
    
    # Créer le répertoire si nécessaire
    $dir = Split-Path $soPath
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
    
    # Écrire le header ELF
    [System.IO.File]::WriteAllBytes($soPath, $elfHeader)
    
    Write-Host "⚠️  Fichier .so stub créé : $soPath" -ForegroundColor Yellow
    Write-Host "   Ce fichier ne fonctionnera pas réellement sur Linux." -ForegroundColor Yellow
    Write-Host "   Utilisez WSL ou un environnement Linux pour créer un vrai .so" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Prochaines étapes :" -ForegroundColor Cyan
Write-Host "1. Testez avec : mvn test" -ForegroundColor White
Write-Host "2. Pour un vrai fichier .so, utilisez Linux ou WSL" -ForegroundColor White

# Vérifier le fichier créé
if (Test-Path $soPath) {
    $fileInfo = Get-Item $soPath
    Write-Host "Fichier créé : $($fileInfo.FullName) ($($fileInfo.Length) bytes)" -ForegroundColor Green
}