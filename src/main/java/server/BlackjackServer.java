package server;

import game.BlackjackGame;
import game.GameState;

import java.util.Locale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BlackjackServer implements AutoCloseable {
    private final ServerSocket serverSocket;
    private volatile Socket currentClient;

    public BlackjackServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void startServer() throws IOException {
        while (!serverSocket.isClosed()) {
            Socket client;
            try {
                client = serverSocket.accept();
            } catch (IOException e) {
                if (serverSocket.isClosed()) {
                    return;
                }
                throw e;
            }
            try (client) {
                currentClient = client;
                if (!serverSocket.isClosed()) {
                    handleClient(client);
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    System.err.println("Connection error: " + e.getMessage());
                }
            } finally {
                currentClient = null;
            }
        }
    }

    private void handleClient(Socket client) throws IOException {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter output = new PrintWriter(client.getOutputStream(), true, StandardCharsets.UTF_8)) {
            BlackjackGame game = new BlackjackGame();
            String command;
            while ((command = input.readLine()) != null) {
                String action = command.trim().toUpperCase(Locale.ROOT);
                if (action.equals("QUIT")) {
                    output.println("Goodbye.");
                    return;
                }
                try {
                    switch (action) {
                        case "START" -> game.startGame();
                        case "HIT" -> game.hit();
                        case "STAND" -> game.stand();
                        default -> {
                            output.println("Unknown command. Use START, HIT, STAND, or QUIT.");
                            continue;
                        }
                    }
                    output.println(formatState(game.getGameState()));
                } catch (IllegalStateException e) {
                    output.println("Invalid action: " + e.getMessage());
                }
            }
        }
    }

    private String formatState(GameState state) {
        String message = "Player: " + state.getPlayerCards() + " (score: " + state.getPlayerScore()
                + "); Dealer: " + state.getDealerCards();
        if (state.getStatus() == GameState.Status.FINISHED) {
            return message + "; Result: " + state.getResult() + ". Enter START for another round.";
        }
        return message + "; Enter HIT or STAND.";
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
        Socket client = currentClient;
        if (client != null) {
            client.close();
        }
    }

    public static void main(String[] args) {
        try {
            int port = args.length == 0 ? 5000 : Integer.parseInt(args[0]);
            try (BlackjackServer server = new BlackjackServer(port)) {
                System.out.println("Server listening on port " + server.getPort());
                server.startServer();
            }
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}
