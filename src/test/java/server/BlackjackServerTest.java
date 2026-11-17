package server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import storage.GameStats;
import storage.StatsStorage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BlackjackServerTest {
    @TempDir Path directory;
    @Test
    void handlesCommandsAndAcceptsAnotherClientAfterDisconnect() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (BlackjackServer server = new BlackjackServer(0, directory.resolve("stats.properties"))) {
            Future<?> running = executor.submit(() -> {
                try {
                    server.startServer();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                try (Socket client = new Socket("127.0.0.1", server.getPort());
                     BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter output = new PrintWriter(client.getOutputStream(), true, StandardCharsets.UTF_8)) {
                    client.setSoTimeout(2000);
                    output.println("HIT");
                    assertTrue(input.readLine().startsWith("Invalid action:"));
                    output.println("invalid");
                    assertTrue(input.readLine().startsWith("Unknown command."));
                    output.println("START");
                    String state = input.readLine();
                    assertTrue(state.contains("Player:"));
                    assertTrue(state.contains("Dealer:"));
                    if (!state.contains("Result:")) {
                        assertTrue(state.contains("Enter HIT or STAND."));
                        output.println("HIT");
                        state = input.readLine();
                        assertTrue(state.contains("Player:"));
                        if (!state.contains("Result:")) {
                            output.println("STAND");
                            state = input.readLine();
                            assertTrue(state.contains("Result:"));
                        }
                    }
                    output.println("STAND");
                    assertTrue(input.readLine().startsWith("Invalid action:"));
                    output.println("START");
                    assertTrue(input.readLine().contains("Player:"));
                    output.println("quit");
                    assertEquals("Goodbye.", input.readLine());
                    assertNull(input.readLine());
                }
                try (Socket client = new Socket("127.0.0.1", server.getPort());
                     BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter output = new PrintWriter(client.getOutputStream(), true, StandardCharsets.UTF_8)) {
                    client.setSoTimeout(2000);
                    output.println("QUIT");
                    assertEquals("Goodbye.", input.readLine());
                }
            } finally {
                server.close();
            }
            running.get(2, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }
    @Test
    void recordsFinishedRoundsOnceAndReloadsStats() throws Exception {
        Path path = directory.resolve("stats.properties");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (BlackjackServer server = new BlackjackServer(0, path)) {
            Future<?> running = executor.submit(() -> {
                try {
                    server.startServer();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            try (Socket client = new Socket("127.0.0.1", server.getPort());
                 BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter output = new PrintWriter(client.getOutputStream(), true, StandardCharsets.UTF_8)) {
                client.setSoTimeout(2000);
                output.println("STATS");
                assertTrue(input.readLine().startsWith("Games: 0;"));
                for (int round = 1; round <= 2; round++) {
                    output.println("START");
                    String response = input.readLine();
                    if (!response.contains("Result:")) {
                        output.println("STAND");
                        response = input.readLine();
                    }
                    assertTrue(response.contains("Result:"));
                    output.println("STATS");
                    assertTrue(input.readLine().startsWith("Games: " + round + ";"));
                    output.println("STAND");
                    assertTrue(input.readLine().startsWith("Invalid action:"));
                    output.println("STATS");
                    assertTrue(input.readLine().startsWith("Games: " + round + ";"));
                }
            } finally {
                server.close();
            }
            running.get(2, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
        GameStats saved = new StatsStorage(path).loadStats();
        assertEquals(2, saved.gamesPlayed());
        ExecutorService restartExecutor = Executors.newSingleThreadExecutor();
        try (BlackjackServer restarted = new BlackjackServer(0, path)) {
            Future<?> running = restartExecutor.submit(() -> {
                try {
                    restarted.startServer();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            try (Socket client = new Socket("127.0.0.1", restarted.getPort());
                 BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter output = new PrintWriter(client.getOutputStream(), true, StandardCharsets.UTF_8)) {
                client.setSoTimeout(2000);
                output.println("STATS");
                assertTrue(input.readLine().startsWith("Games: 2;"));
            } finally {
                restarted.close();
            }
            running.get(2, TimeUnit.SECONDS);
        } finally {
            restartExecutor.shutdownNow();
        }
    }
}
