package test.java;

import main.java.BLEServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

class BLEServerTest {

    // Fake BLEServer for tests (no real DLL)
    static class FakeBLEServer extends BLEServer {
        int notifyCalls = 0;
        boolean failOnNotify = false;
        int startResult = 0; // native result

        @Override
        protected int nativeStartServer(String serviceUuid, String charUuid) {
            return startResult;
        }

        @Override
        protected void nativeStopServer() {
            // no-op
        }

        @Override
        protected int nativeNotify(byte[] data) {
            notifyCalls++;
            if (failOnNotify) {
                return -1; // simulate native error
            }
            return 0; // simulate success
        }
    }

    @Test
    @DisplayName("startServer should convert native result 0 to 1")
    void testStartServerSuccess() {
        FakeBLEServer server = new FakeBLEServer();
        server.startResult = 0;
        assertThat(server.startServer("svc", "char")).isEqualTo(1);
    }

    @Test
    @DisplayName("startServer should convert nonzero native result to 0")
    void testStartServerFailure() {
        FakeBLEServer server = new FakeBLEServer();
        server.startResult = -5;
        assertThat(server.startServer("svc", "char")).isEqualTo(0);
    }

    @Test
    @DisplayName("stopServer should call nativeStopServer without exceptions")
    void testStopServer() {
        FakeBLEServer server = new FakeBLEServer();
        // no exception expected
        server.stopServer();
    }

    @Test
    @DisplayName("notify should delegate to nativeNotify")
    void testNotify() {
        FakeBLEServer server = new FakeBLEServer();
        int result = server.notify("abc".getBytes());
        assertThat(result).isZero();
        assertThat(server.notifyCalls).isEqualTo(1);
    }

    @Test
    @DisplayName("sendData should split data into 200-byte chunks and call notify")
    void testSendDataChunkingSuccess() {
        FakeBLEServer server = new FakeBLEServer();
        String bigData = "x".repeat(450);
        boolean result = server.sendData(bigData);
        assertThat(result).isTrue();
        assertThat(server.notifyCalls).isEqualTo((int) Math.ceil(450.0 / 200.0));
    }

    @Test
    @DisplayName("sendData should return false if notify fails")
    void testSendDataNotifyFailure() {
        FakeBLEServer server = new FakeBLEServer();
        server.failOnNotify = true;
        boolean result = server.sendData("hello");
        assertThat(result).isFalse();
        assertThat(server.notifyCalls).isEqualTo(1);
    }

    @Test
    @DisplayName("sendData should handle empty string and return false")
    void testSendDataEmpty() {
        FakeBLEServer server = new FakeBLEServer();
        assertThat(server.sendData("")).isFalse();
        assertThat(server.notifyCalls).isZero();
    }

    @Test
    @DisplayName("sendData should handle exception gracefully and return false")
    void testSendDataExceptionHandling() {
        FakeBLEServer server = new FakeBLEServer() {
            @Override
            protected int nativeNotify(byte[] data) {
                throw new RuntimeException("boom");
            }
        };
        assertThat(server.sendData("test")).isFalse();
    }

    @Test
    @DisplayName("loadNativeLibrary should throw RuntimeException when fallback fails")
    void testLoadNativeLibraryFailure() throws Exception {
        // Get the method via reflection
        var method = BLEServer.class.getDeclaredMethod("loadNativeLibrary");
        method.setAccessible(true);
        BLEServer evil = new BLEServer() {
            @Override
            protected void loadNativeLibrary() {
                try {
                    throw new UnsatisfiedLinkError("forced");
                } catch (UnsatisfiedLinkError e1) {
                    try {
                        throw new IOException("simulated failure");
                    } catch (Exception e2) {
                        throw new RuntimeException("Impossible de charger BLEServer.dll", e2);
                    }
                }
            }
        };

        assertThatThrownBy(() -> {
            try {
                method.invoke(evil);
            } catch (Exception e) {
                throw e.getCause();
            }
        })
        .isInstanceOf(RuntimeException.class)
        .hasCauseInstanceOf(IOException.class);
    }
}
