package com.cli.chat.server;
// ChatServer.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 5000;
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                pool.execute(handler);
            }
        }
    }

    static void broadcast(String msg, ClientHandler sender) {
        for (ClientHandler c : clients) {
            if (c != sender) c.send(msg);
        }
    }

    static void remove(ClientHandler c) {
        clients.remove(c);
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private String name = "anon";

        ClientHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Enter your name:");
                name = in.readLine();
                if (name == null || name.isBlank()) name = "anon";
                broadcast("*** " + name + " joined ***", this);

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("/quit")) break;
                    broadcast("[" + name + "] " + line, this);
                }
            } catch (IOException e) {
                // client dropped
            } finally {
                close();
            }
        }

        void send(String msg) {
            if (out != null) out.println(msg);
        }

        private void close() {
            remove(this);
            broadcast("*** " + name + " left ***", this);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}