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

    // Optional: Clean up session (e.g., on logout or exit)
    public static void clearSession() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        clientSocket = null;
        currentUsername = null;
    }
}
