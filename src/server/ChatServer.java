package server;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Simple multi-client chat server.
 */
public class ChatServer {

    private static final int PORT = 12345;
    private static final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        System.out.println("Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected.");

                ClientHandler handler = new ClientHandler(clientSocket, clientWriters);
                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
