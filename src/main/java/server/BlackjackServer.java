package server;

import game.BlackjackGame;
import game.GameState;
import storage.GameStats;
import storage.StatsStorage;

import java.util.Locale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class BlackjackServer implements AutoCloseable {
    private final ServerSocket serverSocket;
    private volatile Socket currentClient;
    private final StatsStorage storage;
    private GameStats stats;
    private boolean statsFailed;

    public BlackjackServer(int port) throws IOException {
        this(port, Path.of(System.getProperty("user.home"), ".blackjack-stats.properties"));
    }

    public BlackjackServer(int port, Path statsPath) throws IOException {
        storage = new StatsStorage(statsPath);
        stats = storage.loadStats();
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
                if (action.equals("STATS")) {
                    output.println("Games: " + stats.gamesPlayed() + "; Wins: " + stats.wins()
                            + "; Losses: " + stats.losses() + "; Pushes: " + stats.pushes());
                    continue;
                }
                if (statsFailed) {
                    output.println("Statistics could not be saved. Restart the server after checking storage.");
                    continue;
                }
                try {
                    switch (action) {
                        case "START" -> game.startGame();
                        case "HIT" -> game.hit();
                        case "STAND" -> game.stand();
                        default -> {
                            output.println("Unknown command. Use START, HIT, STAND, STATS, or QUIT.");
                            continue;
                        }
                    }
                    GameState state = game.getGameState();
                    String response = formatState(state);
                    if (state.getStatus() == GameState.Status.FINISHED) {
                        try {
                            saveResult(state.getResult());
                        } catch (IOException | IllegalArgumentException | ArithmeticException e) {
                            statsFailed = true;
                            System.err.println("Could not save statistics: " + e.getMessage());
                            response += " Statistics could not be saved; ask the server operator to check storage.";
                        }
                    }
                    output.println(response);
                } catch (IllegalStateException e) {
                    output.println("Invalid action: " + e.getMessage());
                }
            }
        }
    }

    private void saveResult(GameState.Result result) throws IOException {
        GameStats updated = new GameStats(
                Math.addExact(stats.wins(), result == GameState.Result.PLAYER_WIN ? 1 : 0),
                Math.addExact(stats.losses(), result == GameState.Result.DEALER_WIN ? 1 : 0),
                Math.addExact(stats.pushes(), result == GameState.Result.PUSH ? 1 : 0));
        storage.saveStats(updated);
        stats = updated;
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
