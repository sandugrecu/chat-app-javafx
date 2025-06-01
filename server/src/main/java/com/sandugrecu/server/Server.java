package com.sandugrecu.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private final ServerSocket serverSocket;
	private final static int port = 12345;
	
	public Server(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public void startServer() {
	    System.out.println("[INFO] Server is running and waiting for connections... (SERVER)" + "\n");

		while (!serverSocket.isClosed()) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println("[INFO] A new client has connected. (SERVER)");

				ClientHandler clientHandler = new ClientHandler(socket);
				new Thread(clientHandler).start();

			} catch (IOException e) {
				if (serverSocket.isClosed()) {
					System.out.println("[INFO] Server socket is closed. Stopping server...");
				} else {
					System.err.println("[ERROR] Error accepting client connection: " + e.getMessage());
					e.printStackTrace(System.err);
				}
			}
		}
	}

	public void closeServerSocket() {
		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
				System.out.println("[INFO] Server socket successfully closed.");
			} catch (IOException e) {
				System.err.println("[ERROR] Failed to close server socket: " + e.getMessage());
				e.printStackTrace(System.err);
			}
		} else {
			System.out.println("[INFO] Server socket was already null or closed.");
		}
	}

	public static void main(String[] args) {
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			Server server = new Server(serverSocket);
			Runtime.getRuntime().addShutdownHook(new Thread(server::closeServerSocket));
			server.startServer();
		} catch (IOException e) {
			System.err.println("[ERROR] Could not start server: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}

}