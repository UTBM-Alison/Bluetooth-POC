/**
 * Vital BLE - Ultra simple avec UUIDs configurables
 */
public class VitalBLE {
    
    private static BLEServer server = new BLEServer();
    private static boolean isStarted = false;
    
    // UUIDs par défaut (Heart Rate Service)
    private static String serviceUuid = "0000180D-0000-1000-8000-00805F9B34FB";
    private static String charUuid = "00002A37-0000-1000-8000-00805F9B34FB";
    
    /**
     * Configurer les UUIDs avant le premier envoi
     * @param serviceUuid UUID du service BLE
     * @param characteristicUuid UUID de la caractéristique
     */
    public static void configure(String serviceUuid, String characteristicUuid) {
        if (isStarted) {
            throw new IllegalStateException("Cannot configure UUIDs after server is started. Call configure() before send()");
        }
        
        if (serviceUuid != null && !serviceUuid.trim().isEmpty()) {
            VitalBLE.serviceUuid = serviceUuid.trim();
        }
        
        if (characteristicUuid != null && !characteristicUuid.trim().isEmpty()) {
            VitalBLE.charUuid = characteristicUuid.trim();
        }
    }
    
    /**
     * MÉTHODE PRINCIPALE - Envoyer données
     */
    public static boolean send(String data) {
        if (!isStarted) {
            start();
        }
        
        if (data != null && isStarted) {
            return server.sendData(data);
        }
        return false;
    }
    
    /**
     * Démarrer serveur BLE avec les UUIDs configurés
     */
    private static void start() {
        if (!isStarted) {
            int result = server.startServer(serviceUuid, charUuid);
            isStarted = (result == 0);
        }
    }
    
    /**
     * Obtenir les UUIDs actuellement configurés
     * @return String avec les UUIDs configurés
     */
    public static String getConfiguration() {
        return String.format("Service UUID: %s\nCharacteristic UUID: %s\nServer started: %s", 
                           serviceUuid, charUuid, isStarted);
    }
    
    /**
     * Arrêter serveur
     */
    public static void shutdown() {
        if (isStarted) {
            server.stopServer();
            isStarted = false;
        }
    }
}