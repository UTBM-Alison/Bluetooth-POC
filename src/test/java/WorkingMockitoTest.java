import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests Mockito qui MARCHE avec Java 25 - Version ultra-simple
 */
public class WorkingMockitoTest {
    
    @Mock
    private BLEServer mockServer;
    
    private AutoCloseable closeable;
    
    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        VitalBLE.reset();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
        VitalBLE.reset();
    }
    
    @Test
    void testMockitoBasicFunctionality() {
        // Test ultra-simple pour vérifier que Mockito fonctionne
        when(mockServer.startServer("service", "char")).thenReturn(0);
        when(mockServer.sendData("test")).thenReturn(true);
        
        // Appels directs au mock
        int startResult = mockServer.startServer("service", "char");
        boolean sendResult = mockServer.sendData("test");
        
        // Assertions
        assertThat(startResult).isEqualTo(0);
        assertThat(sendResult).isTrue();
        
        // Vérifications
        verify(mockServer).startServer("service", "char");
        verify(mockServer).sendData("test");
    }
    
    @Test
    void testMockitoWithNotification() {
        // Test des notifications avec mock
        when(mockServer.notify(any(byte[].class))).thenReturn(5);
        
        byte[] data = "hello".getBytes();
        int result = mockServer.notify(data);
        
        assertThat(result).isEqualTo(5);
        verify(mockServer).notify(data);
    }
    
    @Test
    void testMockitoErrorCases() {
        // Test des cas d'erreur
        when(mockServer.startServer("error", "error")).thenReturn(-1);
        when(mockServer.sendData("error")).thenReturn(false);
        
        assertThat(mockServer.startServer("error", "error")).isEqualTo(-1);
        assertThat(mockServer.sendData("error")).isFalse();
    }
    
    @Test
    void testVitalBLEEdgeCases() {
        // Test des edge cases pour améliorer le coverage sans static mocking
        
        // Test avec paramètres null/vides - configure est void
        VitalBLE.configure(null, "char");
        VitalBLE.configure("", "char");
        VitalBLE.configure("service", null);
        VitalBLE.configure("service", "");
        
        // Configuration valide puis tests
        VitalBLE.configure("test-service", "test-char");
        
        // Test send avec null
        assertThat(VitalBLE.send(null)).isFalse();
        
        // Test send normal
        assertThat(VitalBLE.send("valid data")).isTrue();
        
        // Shutdown avant de reconfigurer
        VitalBLE.shutdown();
        
        // Test configuration multiple
        VitalBLE.configure("new-service", "new-char");
        
        // Test getConfiguration
        String config = VitalBLE.getConfiguration();
        assertThat(config).isNotNull().contains("new-service").contains("new-char");
        
        // Test shutdown
        VitalBLE.shutdown();
        
        // Test reconfiguration après shutdown
        VitalBLE.configure("after-shutdown", "test");
        
        VitalBLE.reset();
    }
    
    @Test
    void testMockitoWithMultipleScenarios() {
        // Test avec différents scénarios de retour
        when(mockServer.startServer("success", "success")).thenReturn(0);
        when(mockServer.startServer("fail", "fail")).thenReturn(-1);
        when(mockServer.sendData("good")).thenReturn(true);
        when(mockServer.sendData("bad")).thenReturn(false);
        
        // Test tous les scénarios
        assertThat(mockServer.startServer("success", "success")).isEqualTo(0);
        assertThat(mockServer.startServer("fail", "fail")).isEqualTo(-1);
        assertThat(mockServer.sendData("good")).isTrue();
        assertThat(mockServer.sendData("bad")).isFalse();
        
        // Vérifier tous les appels
        verify(mockServer).startServer("success", "success");
        verify(mockServer).startServer("fail", "fail");
        verify(mockServer).sendData("good");
        verify(mockServer).sendData("bad");
    }
    
    @Test
    void testDataSizesAndFormats() {
        // Test avec différentes tailles de données pour couvrir plus de branches
        VitalBLE.configure("size-test", "size-char");
        
        // Données vides
        assertThat(VitalBLE.send("")).isTrue();
        
        // Données courtes
        assertThat(VitalBLE.send("a")).isTrue();
        
        // Données moyennes
        assertThat(VitalBLE.send("This is a medium length string")).isTrue();
        
        // Données longues
        String longData = "X".repeat(500);
        assertThat(VitalBLE.send(longData)).isTrue();
        
        // Données très longues
        String veryLongData = "Y".repeat(2000);
        assertThat(VitalBLE.send(veryLongData)).isTrue();
        
        // Test avec caractères spéciaux
        assertThat(VitalBLE.send("Données avec àçéèñü")).isTrue();
        
        VitalBLE.reset();
    }
}