import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests JUnit 5 pour BLEServer (interface native)
 */
class BLEServerTest {
    
    private BLEServer bleServer;
    
    @BeforeEach
    void setUp() {
        bleServer = new BLEServer();
    }
    
    @Test
    @DisplayName("Should create BLEServer instance")
    void testBLEServerCreation() {
        assertThat(bleServer).isNotNull();
    }
    
    @Test
    @DisplayName("Should start server with valid UUIDs")
    void testStartServerWithValidUuids() {
        String serviceUuid = "0000180D-0000-1000-8000-00805F9B34FB";
        String charUuid = "00002A37-0000-1000-8000-00805F9B34FB";
        
        int result = bleServer.startServer(serviceUuid, charUuid);
        
        // 0 = succès, autre = erreur
        assertThat(result).isEqualTo(0);
        
        // Nettoyer
        bleServer.stopServer();
    }
    
    @Test
    @DisplayName("Should handle invalid UUIDs gracefully") 
    void testStartServerWithInvalidUuids() {
        assertThatCode(() -> {
            bleServer.startServer("INVALID-UUID", "INVALID-CHAR");
        }).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should send data successfully")
    void testSendData() {
        // Démarrer le serveur d'abord
        bleServer.startServer("0000180D-0000-1000-8000-00805F9B34FB", 
                             "00002A37-0000-1000-8000-00805F9B34FB");
        
        String testData = "Hello BLE!";
        boolean result = bleServer.sendData(testData);
        
        assertThat(result).isTrue();
        
        // Nettoyer
        bleServer.stopServer();
    }
    
    @Test
    @DisplayName("Should handle null data in sendData")
    void testSendNullData() {
        bleServer.startServer("0000180D-0000-1000-8000-00805F9B34FB", 
                             "00002A37-0000-1000-8000-00805F9B34FB");
        
        boolean result = bleServer.sendData(null);
        
        assertThat(result).isFalse();
        
        bleServer.stopServer();
    }
    
    @Test
    @DisplayName("Should handle empty data in sendData")
    void testSendEmptyData() {
        bleServer.startServer("0000180D-0000-1000-8000-00805F9B34FB", 
                             "00002A37-0000-1000-8000-00805F9B34FB");
        
        boolean result = bleServer.sendData("");
        
        assertThat(result).isTrue(); // Empty string should be valid
        
        bleServer.stopServer();
    }
    
    @Test
    @DisplayName("Should stop server safely")
    void testStopServer() {
        // Démarrer puis arrêter
        bleServer.startServer("0000180D-0000-1000-8000-00805F9B34FB", 
                             "00002A37-0000-1000-8000-00805F9B34FB");
        
        assertThatCode(() -> {
            bleServer.stopServer();
        }).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should handle multiple stop calls")
    void testMultipleStopCalls() {
        bleServer.startServer("0000180D-0000-1000-8000-00805F9B34FB", 
                             "00002A37-0000-1000-8000-00805F9B34FB");
        
        assertThatCode(() -> {
            bleServer.stopServer();
            bleServer.stopServer();
            bleServer.stopServer();
        }).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should handle chunked data transmission")
    void testChunkedDataTransmission() {
        bleServer.startServer("0000180D-0000-1000-8000-00805F9B34FB", 
                             "00002A37-0000-1000-8000-00805F9B34FB");
        
        // Test avec des données de différentes tailles
        String smallData = "Small";
        String mediumData = "A".repeat(100);
        String largeData = "B".repeat(500);
        
        assertThat(bleServer.sendData(smallData)).isTrue();
        assertThat(bleServer.sendData(mediumData)).isTrue();
        assertThat(bleServer.sendData(largeData)).isTrue();
        
        bleServer.stopServer();
    }
}
