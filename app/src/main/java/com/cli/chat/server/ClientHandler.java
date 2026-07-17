package com.cli.chat.server;
// ClientHandler.java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
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
            ChatServer.broadcast("*** " + name + " joined ***", this);

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("/quit")) break;
                ChatServer.broadcast("[" + name + "] " + line, this);
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
        ChatServer.remove(this);
        ChatServer.broadcast("*** " + name + " left ***", this);
        try { socket.close(); } catch (IOException ignored) {}
    }
}
