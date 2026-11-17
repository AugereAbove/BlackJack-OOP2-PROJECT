package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BlackjackClient {
    public static void main(String[] args) {
        try {
            String host = args.length > 0 ? args[0] : "localhost";
            int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;
            try (Socket socket = new Socket(host, port);
                 BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter output = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
                System.out.println("Connected. Enter START, HIT, STAND, STATS, or QUIT.");
                String command;
                while ((command = console.readLine()) != null) {
                    output.println(command);
                    String response = input.readLine();
                    if (response == null) {
                        System.out.println("Server disconnected.");
                        break;
                    }
                    System.out.println(response);
                    if (command.trim().equalsIgnoreCase("QUIT")) {
                        break;
                    }
                }
            }
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}
