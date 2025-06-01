package com.sandugrecu.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sandugrecu.database.DatabaseConnection;

public class ClientHandler implements Runnable {
	
	public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
	 // Map of active listeners per chat pair (key = "user1:user2" sorted lexicographically)
    private static final Map<String, List<ClientHandler>> chatListeners = Collections.synchronizedMap(new HashMap<>());
    
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String clientUsername;

    private String listeningChatKey = null;
	
	public ClientHandler(Socket socket) {
		try {
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.clientUsername = bufferedReader.readLine();
			
			System.out.println("[INFO] Client handler created for: " + clientUsername + " (ClientHandler)\n");

			//adding the client to the clients list
			clientHandlers.add(this);
		}catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}
	
	@Override 
	public void run() {
	    String clientRequest;

	    try {
	        while ((clientRequest = bufferedReader.readLine()) != null) {

				//prints to the console every request received from the client
	        	if (!clientRequest.startsWith("GET_CHAT")) {
	        		System.out.println("[INFO] Received from client: " + clientRequest + " (ClientHandler)");	        		
	        	}
	            try {
					handleRequest(clientRequest);
				} catch (ClassNotFoundException e) {
					System.err.println("[ERROR] Class not found: " + e.getMessage());
				}
	        }
	    } catch (IOException e) {
	        System.out.println("Client " + clientUsername + " disconnected.");
	        closeEverything(socket, bufferedReader, bufferedWriter);
	    }
	}
	
	private void handleRequest(String request) throws IOException, ClassNotFoundException {
	    String[] parts = request.split(":", 2);
	    String command = parts[0];

	    switch (command) {
	        case "LOGIN":
	            String[] loginData = parts[1].split(":");
	            String loginUser = loginData[0];
	            String loginPassword = loginData[1];

				//verifies if the login is valid
	            boolean isValid = DatabaseConnection.validateUser(loginUser, loginPassword);
	            if (isValid) {
	                this.clientUsername = loginUser;
	                sendMessage("LOGIN_SUCCESS");
	            } else {
	                sendMessage("LOGIN_FAIL");
	            }
	            break;

	        case "REGISTER":
	            String[] regData = parts[1].split(":");	            
	            String regUser = regData[0];
	            String regPass = regData[1];
	            String repeatedPassword = regData[2];
	            
	            //verifies if the username is available
	            if (!DatabaseConnection.isUsernameAvailable(regUser)) {
	            	sendMessage("REGISTER_FAIL:Username is not available!");
	            	break;
	            }
	            
	            //verifies if both password entryes are the same
	            if (!regPass.equals(repeatedPassword)) {
	            	sendMessage("REGISTER_FAIL:Passwords do NOT match!");
	            	break;
	            }
	            
	            boolean registered = DatabaseConnection.createUser(regUser, regPass);
	            sendMessage(registered ? "REGISTER_SUCCESS" : "REGISTER_FAIL");
	            break;

	        case "GET_CONTACTS":
	            ArrayList<String> contacts = DatabaseConnection.getContacts(parts[1]);
	            for (String contact : contacts) {
	                sendMessage("CONTACT:" + contact);
	            }
	            sendMessage("CONTACTS_DONE");
	            break;

	        case "SEND_MESSAGE":
                // Expected format: SEND_MESSAGE:fromUser:toUser:message
                String[] msgData = parts[1].split(":", 3);
                String fromUser = msgData[0];
                String toUser = msgData[1];
                String msg = msgData[2];

                // Store message in DB
                DatabaseConnection.storeMessage(fromUser, toUser, msg);

                // Broadcast to all listeners of this chat
                String chatKey = getChatKey(fromUser, toUser);

                synchronized (chatListeners) {
                    List<ClientHandler> listeners = chatListeners.get(chatKey);
                    if (listeners != null) {
                        String messagePayload = "MESSAGE:" + fromUser + ": " + msg;
                        for (ClientHandler listener : listeners) {
                            try {
                                listener.sendMessage(messagePayload);
                            } catch (IOException e) {
                                // If client is disconnected, remove from listeners
                                e.printStackTrace(System.err);
                                listener.stopListening();
                            }
                        }
                    }
                }
                break;
            
	        case "ADD_CONTACT":
	            String[] addContactData = parts[1].split(":");
	            if (addContactData.length != 2) {
	                sendMessage("ADD_CONTACT_FAIL:Invalid format");
	                break;
	            }
	            
	            String user1 = addContactData[0];
	            String user2 = addContactData[1];
	            
	            // Ensure both users are not the same
	            if (user1.equals(user2)) {
	                sendMessage("ADD_CONTACT_FAIL:You cannot add yourself.");
	                break;
	            }

	            // Attempt to add contact
	            String result = DatabaseConnection.addContact(user1, user2);
	            System.out.println(result + "(Server)");
	            // Send back the result (success or failure message)
	            sendMessage(result);
	            break;
	            
	        case "GET_CHAT":
	            String[] getChatData = parts[1].split(":");
	            if (getChatData.length != 2) {
	                sendMessage("GET_CHAT_FAIL:Invalid format");
	                break;
	            }

	            String userA = getChatData[0];
	            String userB = getChatData[1];

	            ArrayList<String> messages = DatabaseConnection.getMessagesBetweenUsers(userA, userB);
	            for (String message : messages) {
	                sendMessage("MESSAGE:" + message);
	            }
	            sendMessage("CHAT_DONE");
	            break;
	            
	        case "START_LISTEN":
                // Expected format: START_LISTEN:user:contact
                String[] listenData = parts[1].split(":");
                if (listenData.length != 2) {
                    sendMessage("START_LISTEN_FAIL:Invalid format");
                    break;
                }
                String userA1 = listenData[0];
                String userB2 = listenData[1];

                listeningChatKey = getChatKey(userA1, userB2);
                registerListener(listeningChatKey, this);
                sendMessage("START_LISTEN_SUCCESS");
                break;
                
	        default:
	            sendMessage("UNKNOWN_COMMAND");
	    }
	}

	private void sendMessage(String message) throws IOException {
	    bufferedWriter.write(message);
	    bufferedWriter.newLine();
	    bufferedWriter.flush();
	}
	
	// Utility to create a unique chat key (sorted to handle both directions)
    private String getChatKey(String userA, String userB) {
        if (userA.compareTo(userB) < 0) {
            return userA + ":" + userB;
        } else {
            return userB + ":" + userA;
        }
    }

    // Register a client handler as listener for a chat
    private void registerListener(String chatKey, ClientHandler clientHandler) {
        synchronized (chatListeners) {
            chatListeners.computeIfAbsent(chatKey, _ -> new ArrayList<>());
            List<ClientHandler> listeners = chatListeners.get(chatKey);
            if (!listeners.contains(clientHandler)) {
                listeners.add(clientHandler);
                System.out.println("[INFO] Registered listener for chat " + chatKey + " (User: " + clientHandler.clientUsername + ")");
            }
        }
    }

    // Remove the client from listening lists and clean up
    private void stopListening() {
        if (listeningChatKey != null) {
            synchronized (chatListeners) {
                List<ClientHandler> listeners = chatListeners.get(listeningChatKey);
                if (listeners != null) {
                    listeners.remove(this);
                    if (listeners.isEmpty()) {
                        chatListeners.remove(listeningChatKey);
                    }
                }
            }
            listeningChatKey = null;
        }
    }

	
	public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
	    clientHandlers.remove(this);

		try {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
			if (socket != null) {
				socket.close();
			}
		}catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}
}
