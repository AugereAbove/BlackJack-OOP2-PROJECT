package client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import server.BlackjackServer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BlackjackClientTest {
    @TempDir Path directory;

    @Test
    void consoleClientPlaysAndQuits() throws Exception {
        var executor = Executors.newSingleThreadExecutor();
        try (var server = new BlackjackServer(0, directory.resolve("stats.properties"))) {
            var running = executor.submit(() -> {
                try {
                    server.startServer();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
                String classes = Path.of(BlackjackClient.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI()).toString();
                Process client = new ProcessBuilder(java, "-cp", classes,
                        "client.BlackjackClient", "127.0.0.1", Integer.toString(server.getPort()))
                        .redirectErrorStream(true).start();
                try {
                    client.getOutputStream().write("START\nSTAND\nSTATS\nQUIT\n".getBytes(StandardCharsets.UTF_8));
                    client.getOutputStream().close();
                    assertTrue(client.waitFor(10, TimeUnit.SECONDS));
                    String output = new String(client.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    assertEquals(0, client.exitValue());
                    assertTrue(output.contains("Player:"), output);
                    assertTrue(output.contains("Result:"), output);
                    assertTrue(output.contains("Games: 1;"), output);
                    assertTrue(output.contains("Goodbye."), output);
                } finally {
                    client.destroyForcibly();
                }
            } finally {
                server.close();
            }
            running.get(2, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }
}
