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
        String libraryName = "BLEServer.dll";
        InputStream inputStream = BLEServer.class.getClassLoader().getResourceAsStream(libraryName);
        
        if (inputStream == null) {
            throw new FileNotFoundException("BLEServer.dll non trouvé dans les resources");
        }
        
        // Créer un fichier temporaire
        Path tempFile = Files.createTempFile("BLEServer", ".dll");
        tempFile.toFile().deleteOnExit();
        
        // Copier la DLL vers le fichier temporaire
        try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
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