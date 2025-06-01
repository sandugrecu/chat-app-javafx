package com.sandugrecu.client;


public class Session {
    private static ClientSocket clientSocket;
    private static String currentUsername;

    // Setters
    public static void setClientSocket(ClientSocket socket) {
        clientSocket = socket;
    }
    public static void setCurrentUsername(String username) {
        currentUsername = username;
    }

    // Getters
    public static ClientSocket getClientSocket() {
        return clientSocket;
    }
    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void clearSession() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        clientSocket = null;
        currentUsername = null;
    }
}
