import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests JUnit 5 modernes pour VitalBLE avec JaCoCo
 */
public class VitalBLETest {
    
    @BeforeEach
    void setUp() {
        // S'assurer d'un état propre avant chaque test
        VitalBLE.reset();
    }
    
    @AfterEach 
    void tearDown() {
        // Nettoyer après chaque test
        VitalBLE.reset();
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
    
    @Test
    @DisplayName("Should handle configure with null service UUID only")
    void testConfigureWithNullServiceUuid() {
        VitalBLE.reset();
        
        // Test avec serviceUuid null mais charUuid valide
        VitalBLE.configure(null, "87654321-4321-4321-4321-CBA987654321");
        
        String config = VitalBLE.getConfiguration();
        assertThat(config)
            .contains("0000180D-0000-1000-8000-00805F9B34FB") // UUID par défaut conservé
            .contains("87654321-4321-4321-4321-CBA987654321"); // Nouveau char UUID
    }
    
    @Test
    @DisplayName("Should handle configure with null char UUID only")  
    void testConfigureWithNullCharUuid() {
        VitalBLE.reset();
        
        // Test avec charUuid null mais serviceUuid valide
        VitalBLE.configure("12345678-1234-1234-1234-123456789ABC", null);
        
        String config = VitalBLE.getConfiguration();
        assertThat(config)
            .contains("12345678-1234-1234-1234-123456789ABC") // Nouveau service UUID
            .contains("00002A37-0000-1000-8000-00805F9B34FB"); // UUID par défaut conservé
    }
    
    @Test
    @DisplayName("Should handle configure with empty string UUIDs")
    void testConfigureWithEmptyStringUuids() {
        VitalBLE.reset();
        
        // Test avec des chaînes vides
        VitalBLE.configure("", "");
        
        String config = VitalBLE.getConfiguration();
        assertThat(config)
            .contains("0000180D-0000-1000-8000-00805F9B34FB") // UUIDs par défaut conservés
            .contains("00002A37-0000-1000-8000-00805F9B34FB");
            
        // Test avec des chaînes contenant seulement des espaces
        VitalBLE.configure("   ", "   ");
        
        config = VitalBLE.getConfiguration();
        assertThat(config)
            .contains("0000180D-0000-1000-8000-00805F9B34FB") // UUIDs par défaut conservés
            .contains("00002A37-0000-1000-8000-00805F9B34FB");
    }
    
    @Test
    @DisplayName("Should handle shutdown when server is not started")
    void testShutdownWhenNotStarted() {
        VitalBLE.reset();
        
        // Appeler shutdown sans démarrer le serveur
        VitalBLE.shutdown();
        
        String config = VitalBLE.getConfiguration();
        assertThat(config).contains("Server started: false");
        
        // Multiples appels shutdown sur serveur non démarré
        VitalBLE.shutdown();
        VitalBLE.shutdown();
        
        config = VitalBLE.getConfiguration();
        assertThat(config).contains("Server started: false");
    }
    
    @Test
    @DisplayName("Should handle send with null data after server started")
    void testSendNullDataAfterServerStarted() {
        VitalBLE.reset();
        
        // Démarrer le serveur avec une donnée valide
        boolean startResult = VitalBLE.send("Start server");
        assertThat(startResult).isTrue();
        
        // Vérifier que le serveur est démarré
        String config = VitalBLE.getConfiguration();
        assertThat(config).contains("Server started: true");
        
        // Maintenant essayer d'envoyer null
        boolean nullResult = VitalBLE.send(null);
        assertThat(nullResult).isFalse();
    }
    
    @Test
    @DisplayName("Should test all branches in send method")
    void testSendMethodBranches() {
        VitalBLE.reset();
        
        // Branch 1: serveur non démarré, data non null -> démarre serveur et envoie
        boolean result1 = VitalBLE.send("First data");
        assertThat(result1).isTrue();
        
        // Branch 2: serveur démarré, data non null -> envoie directement
        boolean result2 = VitalBLE.send("Second data");
        assertThat(result2).isTrue();
        
        // Branch 3: data null -> return false
        boolean result3 = VitalBLE.send(null);
        assertThat(result3).isFalse();
        
        // Arrêter le serveur pour tester la prochaine branche
        VitalBLE.shutdown();
        
        // Branch 4: serveur arrêté, data null -> return false (pas de start)
        boolean result4 = VitalBLE.send(null);
        assertThat(result4).isFalse();
    }
    
    @Test
    @DisplayName("Should test start method branch coverage")
    void testStartMethodBranches() {
        VitalBLE.reset();
        
        // En mode test, on teste la branche BLEServer.isTestMode() == true
        if (BLEServer.isTestMode()) {
            // Test que le serveur se démarre correctement en mode test
            boolean result = VitalBLE.send("Test start");
            assertThat(result).isTrue();
            
            String config = VitalBLE.getConfiguration();
            assertThat(config).contains("Server started: true");
        }
        
        // Note: La branche else (mode réel) est difficile à tester sans désactiver BLE_TEST_MODE
        // mais elle est couverte quand les tests tournent en mode réel
    }
    
    @Test
    @DisplayName("Should ensure configuration coverage with trimming")
    void testConfigurationTrimming() {
        VitalBLE.reset();
        
        // Test avec des espaces en début/fin
        VitalBLE.configure("  12345678-1234-1234-1234-123456789ABC  ", 
                          "  87654321-4321-4321-4321-CBA987654321  ");
        
        String config = VitalBLE.getConfiguration();
        assertThat(config)
            .contains("12345678-1234-1234-1234-123456789ABC") // Pas d'espaces
            .contains("87654321-4321-4321-4321-CBA987654321"); // Pas d'espaces
    }
}
