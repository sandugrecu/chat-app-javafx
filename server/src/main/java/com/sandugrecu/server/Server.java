package com.sandugrecu.server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private ServerSocket serverSocket;
	private static int port = 12345;
	
	public Server(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	public void startServer() {
	    System.out.println("[INFO] Server is running and waiting for connections... (SERVER)" + "\n");

	    try {
	        while (!serverSocket.isClosed()) {
	            Socket socket = serverSocket.accept();
	            System.out.println("[INFO] Un nou client s-a conectat. (SERVER)");
	            ClientHandler clientHandler = new ClientHandler(socket);

	            Thread thread = new Thread(clientHandler);
	            thread.start();
	        }
	    } catch (IOException e) {
	        System.out.println("[DEBUG] Server exception: " + e.getMessage() + " ((SERVER))");
	        e.printStackTrace();
	    }
	}
	
	public void closeServerSocket() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException{
		ServerSocket serverSocket = new ServerSocket(port);
		Server server = new Server(serverSocket);
		server.startServer();
	}
}