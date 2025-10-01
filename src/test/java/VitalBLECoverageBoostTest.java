import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.assertj.core.api.Assertions.*;
import main.java.VitalBLE;

/**
 * Tests spécialement pour atteindre 90%+ coverage sur VitalBLE
 */
public class VitalBLECoverageBoostTest {

    @BeforeEach
    void setUp() {
        VitalBLE.reset();
    }

    @AfterEach
    void tearDown() {
        VitalBLE.reset();
    }

    @Test
    void testConfigureWithNullServiceUuid() {
        // Test configure avec serviceUuid null - ne devrait pas changer l'UUID par défaut
        String originalConfig = VitalBLE.getConfiguration();
        
        VitalBLE.configure(null, "new-char");
        
        String newConfig = VitalBLE.getConfiguration();
        assertThat(newConfig).contains("0000180D-0000-1000-8000-00805F9B34FB"); // UUID service par défaut
        assertThat(newConfig).contains("new-char"); // UUID char changé
    }

    @Test
    void testConfigureWithEmptyServiceUuid() {
        // Test configure avec serviceUuid vide
        VitalBLE.configure("   ", "new-char");
        
        String config = VitalBLE.getConfiguration();
        assertThat(config).contains("0000180D-0000-1000-8000-00805F9B34FB"); // UUID service par défaut
        assertThat(config).contains("new-char");
    }

    @Test
    void testConfigureWithNullCharUuid() {
        // Test configure avec characteristicUuid null
        VitalBLE.configure("new-service", null);
        
        String config = VitalBLE.getConfiguration();
        assertThat(config).contains("new-service");
        assertThat(config).contains("00002A37-0000-1000-8000-00805F9B34FB"); // UUID char par défaut
    }

    @Test
    void testConfigureWithEmptyCharUuid() {
        // Test configure avec characteristicUuid vide
        VitalBLE.configure("new-service", "  ");
        
        String config = VitalBLE.getConfiguration();
        assertThat(config).contains("new-service");
        assertThat(config).contains("00002A37-0000-1000-8000-00805F9B34FB"); // UUID char par défaut
    }

    @Test
    void testConfigureWithBothNull() {
        // Test configure avec les deux UUID null - aucun changement
        String originalConfig = VitalBLE.getConfiguration();
        
        VitalBLE.configure(null, null);
        
        String newConfig = VitalBLE.getConfiguration();
        assertThat(newConfig).isEqualTo(originalConfig);
    }

    @Test
    void testShutdownWhenNotStarted() {
        // Test shutdown quand le serveur n'est pas démarré - ne devrait rien faire
        VitalBLE.shutdown(); // Premier shutdown (serveur pas démarré)
        VitalBLE.shutdown(); // Deuxième shutdown pour tester la condition if (isStarted)
        
        String config = VitalBLE.getConfiguration();
        assertThat(config).contains("Server started: false");
    }

    @Test
    void testConfigureWithWhitespaceUuids() {
        // Test configure avec des UUIDs qui ont des espaces à trim
        VitalBLE.configure("  service-with-spaces  ", "  char-with-spaces  ");
        
        String config = VitalBLE.getConfiguration();
        assertThat(config).contains("service-with-spaces");
        assertThat(config).contains("char-with-spaces");
        assertThat(config).doesNotContain("  "); // Vérifier que les espaces sont trimés
    }

    @Test
    void testSendAfterMultipleResets() {
        // Test pattern complexe : send -> reset -> send pour couvrir différents chemins
        VitalBLE.send("first");
        VitalBLE.reset();
        VitalBLE.send("second");
        VitalBLE.reset();
        boolean result = VitalBLE.send("third");
        
        assertThat(result).isTrue();
    }

    @Test
    void testGetConfigurationMultipleTimes() {
        // Test getConfiguration dans différents états
        String config1 = VitalBLE.getConfiguration();
        assertThat(config1).contains("Server started: false");
        
        VitalBLE.send("start server");
        String config2 = VitalBLE.getConfiguration();
        assertThat(config2).contains("Server started: true");
        
        VitalBLE.shutdown();
        String config3 = VitalBLE.getConfiguration();
        assertThat(config3).contains("Server started: false");
    }


}