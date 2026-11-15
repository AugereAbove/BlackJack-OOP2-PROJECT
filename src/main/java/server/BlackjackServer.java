package server;

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
            String command;
            while ((command = input.readLine()) != null) {
                if (command.equalsIgnoreCase("QUIT")) {
                    output.println("Goodbye.");
                    return;
                }
                output.println("Unknown command. Enter QUIT to disconnect.");
            }
        }
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
