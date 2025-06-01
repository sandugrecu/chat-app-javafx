package com.sandugrecu.client;


import java.io.*;
import java.net.Socket;

public class ClientSocket {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public ClientSocket(String host, int port) throws IOException {
        socket = new Socket(host, port);
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    // Send a command to the server
    public void sendCommand(String command) throws IOException {
    	System.out.println("[INFO] Sending command: " + command + " (Client Socket)");
        bufferedWriter.write(command);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
    
    public void sendUsername(String username) throws IOException{
    	System.out.println("[INFO] Sending username: " + username + " (Client Socket)");
        bufferedWriter.write(username);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    // Read a line of response from the server
    public String readLine() throws IOException {
        return bufferedReader.readLine();
    }

    // Close the socket and associated streams
    public void close() throws IOException {
        if (bufferedReader != null) {
            bufferedReader.close();
            bufferedReader = null;
        }
        if (bufferedWriter != null) {
            bufferedWriter.close();
            bufferedWriter = null;
        }
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }
}
