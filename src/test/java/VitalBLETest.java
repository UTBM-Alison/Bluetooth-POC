import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests JUnit 5 modernes pour VitalBLE avec JaCoCo
 */
class VitalBLETest {
    
    @BeforeEach
    void setUp() {
        // S'assurer d'un état propre avant chaque test
        VitalBLE.shutdown();
    }
    
    @AfterEach 
    void tearDown() {
        // Nettoyer après chaque test
        VitalBLE.shutdown();
    }
    
    @Test
    @DisplayName("Should have default configuration when not configured")
    void testDefaultConfiguration() {
        String config = VitalBLE.getConfiguration();
        
        assertThat(config)
            .contains("0000180D-0000-1000-8000-00805F9B34FB")
            .contains("00002A37-0000-1000-8000-00805F9B34FB")
            .contains("Server started: false");
    }
    
    @Test
    @DisplayName("Should return configuration details")
    void testGetConfiguration() {
        String config = VitalBLE.getConfiguration();
        
        assertThat(config)
            .isNotNull()
            .isNotEmpty()
            .contains("Service UUID:")
            .contains("Characteristic UUID:")
            .contains("Server started:");
    }
    
    @Test
    @DisplayName("Should configure custom UUIDs successfully")
    void testConfigureCustomUuids() {
        String customService = "12345678-1234-1234-1234-123456789ABC";
        String customChar = "87654321-4321-4321-4321-CBA987654321";
        
        VitalBLE.configure(customService, customChar);
        String config = VitalBLE.getConfiguration();
        
        assertThat(config)
            .contains(customService)
            .contains(customChar);
    }
    
    @Test
    @DisplayName("Should handle null configuration gracefully")
    void testConfigureWithNullValues() {
        assertThatCode(() -> {
            VitalBLE.configure(null, null);
        }).doesNotThrowAnyException();
        
        assertThatCode(() -> {
            VitalBLE.configure("", "");
        }).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should prevent reconfiguration after server start")
    void testPreventReconfigurationAfterStart() {
        // Démarrer le serveur
        VitalBLE.send("test data");
        
        // Essayer de reconfigurer doit lever une exception
        assertThatThrownBy(() -> {
            VitalBLE.configure("NEW-UUID", "NEW-CHAR");
        }).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot configure UUIDs after server is started");
    }
    
    @Test
    @DisplayName("Should return false when sending null data")
    void testSendNullData() {
        boolean result1 = VitalBLE.send(null);
        assertThat(result1).isFalse();
        
        // Démarrer serveur puis tester null
        VitalBLE.send("valid data");
        boolean result2 = VitalBLE.send(null);
        assertThat(result2).isFalse();
    }
    
    @Test
    @DisplayName("Should send data successfully")
    void testSendValidData() {
        boolean result = VitalBLE.send("Hello BLE World!");
        
        assertThat(result).isTrue();
        
        // Vérifier que le serveur est démarré
        String config = VitalBLE.getConfiguration();
        assertThat(config).contains("Server started: true");
    }
    
    @Test
    @DisplayName("Should handle multiple shutdown calls safely")
    void testMultipleShutdownCalls() {
        // Démarrer le serveur
        VitalBLE.send("test");
        
        // Plusieurs appels shutdown ne doivent pas poser problème
        assertThatCode(() -> {
            VitalBLE.shutdown();
            VitalBLE.shutdown();
            VitalBLE.shutdown();
        }).doesNotThrowAnyException();
        
        String config = VitalBLE.getConfiguration();
        assertThat(config).contains("Server started: false");
    }
    
    @Test
    @DisplayName("Should handle large data transmission")
    void testLargeDataTransmission() {
        // Créer une grande chaîne de données
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeData.append("VitalBLE-").append(i).append("-");
        }
        
        boolean result = VitalBLE.send(largeData.toString());
        assertThat(result).isTrue();
    }
}
