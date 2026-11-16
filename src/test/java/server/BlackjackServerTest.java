package server;

import org.junit.jupiter.api.Test;

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
    @Test
    void handlesCommandsAndAcceptsAnotherClientAfterDisconnect() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (BlackjackServer server = new BlackjackServer(0)) {
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
}
