package main.java;

import java.io.*;
import java.nio.file.*;

/**
 * BLE Server - Interface native pour Windows BLE
 * Production implementation that uses native DLL
 */
public class BLEServer implements BLEServerInterface {
    
    static {
        new BLEServer().loadNativeLibrarySafe();
    }
    
    // Instance method wrapping the loader for tests
    protected void loadNativeLibrarySafe() {
        try {
            loadNativeLibrary();
        } catch (RuntimeException e) {
            // In production, just rethrow
            throw e;
        }
    }
    
    protected void loadNativeLibrary() {
        try {
            // D'abord, essayer de charger depuis java.library.path
            System.loadLibrary("BLEServer");
        } catch (UnsatisfiedLinkError e1) {
            try {
                // Si ça échoue, essayer de charger depuis les resources
                String libraryPath = extractLibraryFromResources();
                System.load(libraryPath);
            } catch (Exception e2) {
                throw new RuntimeException("Impossible de charger BLEServer.dll", e2);
            }
        }
    }
    
    private String extractLibraryFromResources() throws IOException {
        // Déterminer l'OS et le nom de la bibliothèque
        String osName = System.getProperty("os.name").toLowerCase();
        String libraryName;
        String fileExtension;
        
        if (osName.contains("win")) {
            libraryName = "BLEServer.dll";
            fileExtension = ".dll";
        } else if (osName.contains("linux")) {
            libraryName = "libBLEServer.so";
            fileExtension = ".so";
        } else {
            throw new UnsupportedOperationException("OS non supporté: " + osName);
        }
        
        // Chercher d'abord dans les resources du classpath
        InputStream inputStream = BLEServer.class.getClassLoader().getResourceAsStream(libraryName);
        
        // Si pas trouvé dans classpath, chercher dans le dossier resources à la racine
        if (inputStream == null) {
            try {
                Path resourcesPath = Paths.get("resources", libraryName);
                if (Files.exists(resourcesPath)) {
                    inputStream = Files.newInputStream(resourcesPath);
                }
            } catch (Exception e) {
                // Ignore et continue avec l'exception originale
            }
        }
        
        if (inputStream == null) {
            throw new FileNotFoundException(libraryName + " non trouvé dans les resources");
        }
        
        // Créer un fichier temporaire avec la bonne extension
        Path tempFile = Files.createTempFile("BLEServer", fileExtension);
        tempFile.toFile().deleteOnExit();
        
        // Copier la bibliothèque vers le fichier temporaire
        try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            inputStream.close();
        }
        
        return tempFile.toAbsolutePath().toString();
    }

    // Méthodes natives
    protected native int nativeStartServer(String serviceUuid, String charUuid);
    protected native void nativeStopServer();
    protected native int nativeNotify(byte[] data);

    // API Java
    @Override
    public int startServer(String serviceUuid, String charUuid) {
        int nativeResult = nativeStartServer(serviceUuid, charUuid);
        // Convert native result (0=success) to standard result (1=success)
        return (nativeResult == 0) ? 1 : 0;
    }
    
    @Override
    public void stopServer() {
        nativeStopServer(); 
    }
    
    @Override
    public int notify(byte[] data) {
        return nativeNotify(data); 
    }
    
    // Envoi rapide
    @Override
    public boolean sendData(String data) {
        try {
            byte[] bytes = data.getBytes("UTF-8");
            
            if (bytes.length == 0) return false;
            
            // Chunks de 200 bytes pour performance max
            int chunkSize = 200;
            
            for (int i = 0; i < bytes.length; i += chunkSize) {
                int end = Math.min(i + chunkSize, bytes.length);
                byte[] chunk = new byte[end - i];
                System.arraycopy(bytes, i, chunk, 0, end - i);
                
                int result = notify(chunk);
                if (result != 0) {
                    return false;
                }
                
                Thread.sleep(1); // Petit délai
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
}