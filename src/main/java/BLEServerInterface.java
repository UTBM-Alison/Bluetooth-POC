package main.java;

/**
 * Interface for BLE Server operations
 * Allows for easy mocking in tests without environment variables
 */
public interface BLEServerInterface {
    
    /**
     * Start the BLE server with specified UUIDs
     * @param serviceUuid Service UUID
     * @param characteristicUuid Characteristic UUID  
     * @return 1 for success, 0 for failure
     */
    int startServer(String serviceUuid, String characteristicUuid);
    
    /**
     * Send notification data
     * @param data Data to send
     * @return 1 for success, 0 for failure
     */
    int notify(byte[] data);
    
    /**
     * Stop the BLE server
     */
    void stopServer();
    
    /**
     * Send data as string (convenience method)
     * @param data String data to send
     * @return true for success, false for failure
     */
    boolean sendData(String data);
}