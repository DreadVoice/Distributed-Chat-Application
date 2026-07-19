package com.cli.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatServer server;
    private PrintWriter out;
    private String name = "anon";

    ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Enter your name:");
            name = in.readLine();
            if (name == null || name.isBlank()) name = "anon";
            server.broadcast("*** " + name + " joined ***", this);

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("/quit")) break;
                server.broadcast("[" + name + "] " + line, this);
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
        server.remove(this);
        server.broadcast("*** " + name + " left ***", this);
        try { socket.close(); } catch (IOException ignored) {}
    }
}