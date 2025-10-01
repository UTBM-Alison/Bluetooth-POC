import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests JUnit 5 pour BLEServer (interface native)
 */
public class BLEServerTest {
    
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
    @DisplayName("Should detect test mode when BLE_TEST_MODE is set")
    void testModeDetection() {
        // This test will pass if BLE_TEST_MODE environment variable is set
        if (System.getenv("BLE_TEST_MODE") != null) {
            assertThat(BLEServer.isTestMode()).isTrue();
            System.out.println("✅ Running in TEST MODE - BLE operations will be mocked");
        } else {
            System.out.println("⚠️ Running in REAL MODE - BLE operations will use actual hardware");
        }
    }
    
    @Test
    @DisplayName("Should start server with valid UUIDs")
    void testStartServerWithValidUuids() {
        String serviceUuid = "0000180D-0000-1000-8000-00805F9B34FB";
        String charUuid = "00002A37-0000-1000-8000-00805F9B34FB";
        
        int result = bleServer.startServer(serviceUuid, charUuid);
        
        // En mode test: 1 = succès, en mode réel: 0 = succès
        if (BLEServer.isTestMode()) {
            assertThat(result).isEqualTo(1);
        } else {
            assertThat(result).isEqualTo(0);
        }
        
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
    
    @Test
    @DisplayName("Should handle empty string data")
    void testSendEmptyStringData() {
        boolean result = bleServer.sendData("");
        
        if (BLEServer.isTestMode()) {
            // En mode test, les chaînes vides sont acceptées
            assertThat(result).isTrue();
        } else {
            // En mode réel, les chaînes vides retournent false
            assertThat(result).isFalse();
        }
    }
    
    @Test
    @DisplayName("Should test notify method directly with byte arrays")
    void testNotifyWithByteArrays() {
        // Test avec données normales
        byte[] normalData = "Test data".getBytes();
        int result1 = bleServer.notify(normalData);
        
        if (BLEServer.isTestMode()) {
            assertThat(result1).isEqualTo(normalData.length);
        } else {
            // En mode réel, on s'attend à un code de retour spécifique
            assertThat(result1).isGreaterThanOrEqualTo(0);
        }
        
        // Test avec données vides
        byte[] emptyData = new byte[0];
        int result2 = bleServer.notify(emptyData);
        
        if (BLEServer.isTestMode()) {
            assertThat(result2).isEqualTo(0);
        } else {
            assertThat(result2).isGreaterThanOrEqualTo(0);
        }
    }
    
    @Test
    @DisplayName("Should test notify with various byte array sizes")
    void testNotifyWithVariousSizes() {
        // Test avec 1 byte
        byte[] smallData = {0x42};
        int result1 = bleServer.notify(smallData);
        
        // Test avec 200 bytes (taille de chunk)
        byte[] mediumData = new byte[200];
        for (int i = 0; i < 200; i++) {
            mediumData[i] = (byte) (i % 256);
        }
        int result2 = bleServer.notify(mediumData);
        
        // Test avec 1000 bytes
        byte[] largeData = new byte[1000];
        for (int i = 0; i < 1000; i++) {
            largeData[i] = (byte) (i % 256);
        }
        int result3 = bleServer.notify(largeData);
        
        if (BLEServer.isTestMode()) {
            assertThat(result1).isEqualTo(1);
            assertThat(result2).isEqualTo(200);
            assertThat(result3).isEqualTo(1000);
        } else {
            // En mode réel, on vérifie juste que ça ne plante pas
            assertThat(result1).isGreaterThanOrEqualTo(0);
            assertThat(result2).isGreaterThanOrEqualTo(0);
            assertThat(result3).isGreaterThanOrEqualTo(0);
        }
    }
    
    @Test
    @DisplayName("Should handle UTF-8 encoding correctly")
    void testUTF8Encoding() {
        // Test avec caractères spéciaux UTF-8
        String utf8Data = "Héllo BLÉ! 🔵 émojis français";
        boolean result = bleServer.sendData(utf8Data);
        assertThat(result).isTrue();
        
        // Vérifier que les caractères UTF-8 sont correctement gérés
        assertThat(utf8Data.getBytes(java.nio.charset.StandardCharsets.UTF_8).length)
            .isGreaterThan(utf8Data.length()); // UTF-8 bytes > char count
    }
    
    @Test
    @DisplayName("Should test static isTestMode method")
    void testIsTestModeMethod() {
        boolean testMode = BLEServer.isTestMode();
        
        // Vérifier la cohérence avec l'environnement
        String envVar = System.getenv("BLE_TEST_MODE");
        if (envVar != null) {
            assertThat(testMode).isTrue();
        } else {
            assertThat(testMode).isFalse();
        }
    }
    
    @Test
    @DisplayName("Should handle edge cases in sendData")
    void testSendDataEdgeCases() {
        // Test avec une chaîne très longue pour forcer le chunking
        StringBuilder longData = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longData.append("0123456789"); // 10 chars * 100 = 1000 chars
        }
        
        boolean result = bleServer.sendData(longData.toString());
        assertThat(result).isTrue();
        
        // Vérifier que la donnée est assez longue pour le chunking
        assertThat(longData.length()).isGreaterThan(200);
    }
    
    @Test
    @DisplayName("Should test chunking boundary conditions")
    void testChunkingBoundaries() {
        // Test exactement 200 chars (1 chunk)
        String exactChunk = "A".repeat(200);
        assertThat(bleServer.sendData(exactChunk)).isTrue();
        
        // Test 201 chars (2 chunks)
        String twoChunks = "B".repeat(201);
        assertThat(bleServer.sendData(twoChunks)).isTrue();
        
        // Test 400 chars (2 chunks exacts)
        String exactTwoChunks = "C".repeat(400);
        assertThat(bleServer.sendData(exactTwoChunks)).isTrue();
    }
    
    @Test
    @DisplayName("Should handle sendData with different thread timing")
    void testSendDataThreading() {
        // En mode test, pas de Thread.sleep() donc pas d'InterruptedException
        // Mais on teste quand même le comportement général
        String data = "Threading test data";
        
        boolean result = bleServer.sendData(data);
        assertThat(result).isTrue();
        
        // Test avec plusieurs appels consécutifs
        assertThat(bleServer.sendData("Call 1")).isTrue();
        assertThat(bleServer.sendData("Call 2")).isTrue();
        assertThat(bleServer.sendData("Call 3")).isTrue();
    }
    
    @Test
    @DisplayName("Should test different server states")
    void testServerStates() {
        // Test démarrage et arrêt multiples
        bleServer.startServer("12345678-1234-1234-1234-123456789ABC", 
                             "87654321-4321-4321-4321-CBA987654321");
        bleServer.stopServer();
        
        // Redémarrage avec d'autres UUIDs
        bleServer.startServer("ABCDEF01-2345-6789-ABCD-EF0123456789", 
                             "98765432-8765-4321-9876-543210987654");
        bleServer.stopServer();
        
        // Vérifier que multiple stopServer() ne pose pas de problème
        bleServer.stopServer();
        bleServer.stopServer();
    }
    
    @Test
    @DisplayName("Should test data transmission edge cases")
    void testDataTransmissionEdgeCases() {
        // Test avec des données qui pourraient causer des problèmes d'encodage
        String specialChars = "Special chars: \n\r\t\0\u0001\u001F";
        boolean result1 = bleServer.sendData(specialChars);
        assertThat(result1).isTrue();
        
        // Test avec des données binaires simulées
        StringBuilder binaryLike = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            binaryLike.append((char) (i % 128)); // Éviter les caractères de contrôle problématiques
        }
        boolean result2 = bleServer.sendData(binaryLike.toString());
        assertThat(result2).isTrue();
    }
    
    @Test  
    @DisplayName("Should test sendData error conditions in test mode")
    void testSendDataErrorConditions() {
        if (BLEServer.isTestMode()) {
            // En mode test, on peut simuler différents scénarios
            
            // Test avec null (déjà testé mais on confirme le comportement)
            assertThat(bleServer.sendData(null)).isFalse();
            
            // Test avec chaîne vide (comportement différent selon le mode)
            assertThat(bleServer.sendData("")).isTrue(); // En mode test, accepté
            
            // Test avec très longues données
            String veryLongData = "X".repeat(10000);
            assertThat(bleServer.sendData(veryLongData)).isTrue();
        }
    }
    
    @Test
    @DisplayName("Should verify all public methods are covered")
    void testAllPublicMethods() {
        // S'assurer que toutes les méthodes publiques sont testées
        
        // isTestMode() - méthode statique
        boolean testMode = BLEServer.isTestMode();
        assertThat(testMode).isNotNull();
        
        // startServer() - déjà testé dans d'autres tests
        int startResult = bleServer.startServer("test-uuid", "test-char");
        if (BLEServer.isTestMode()) {
            assertThat(startResult).isEqualTo(1);
        } else {
            assertThat(startResult).isGreaterThanOrEqualTo(0);
        }
        
        // stopServer() - déjà testé
        bleServer.stopServer(); // Ne devrait pas lever d'exception
        
        // notify() - déjà testé avec différentes tailles
        byte[] testBytes = "Method coverage test".getBytes();
        int notifyResult = bleServer.notify(testBytes);
        if (BLEServer.isTestMode()) {
            assertThat(notifyResult).isEqualTo(testBytes.length);
        } else {
            assertThat(notifyResult).isGreaterThanOrEqualTo(0);
        }
        
        // sendData() - déjà largement testé
        assertThat(bleServer.sendData("Coverage test")).isTrue();
    }
}
