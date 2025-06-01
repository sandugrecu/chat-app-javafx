package com.sandugrecu.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sandugrecu.alertBoxes.AddContactAlert;
import com.sandugrecu.client.ClientSocket;
import com.sandugrecu.client.Config;
import com.sandugrecu.alertBoxes.CustomAlert;
import com.sandugrecu.client.Session;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ContactsController {

    @FXML
    private VBox contactsContainer;

    private String currentUsername;
    private String serverIP = Config.getServerIP();

    public void setUsername(String username) {
        this.currentUsername = username;
        loadContactsFromServer();
    }

    private void loadContactsFromServer() {
        try {
            contactsContainer.getChildren().clear();  
        	
            //ClientSocket clientSocket = new ClientSocket(serverIP, 12345);
            ClientSocket clientSocket = Session.getClientSocket();
            //System.out.println("[DEBUG] ClientSocket: " + Session.getClientSocket());

            // Identify the client to the server
            clientSocket.sendUsername(currentUsername);
            
            // Ask for the contact list
            clientSocket.sendCommand("GET_CONTACTS:" + currentUsername);

            String response;
            while (!(response = clientSocket.readLine()).equals("CONTACTS_DONE")) {
                if (response.startsWith("CONTACT:")) {
                    String contactName = response.substring("CONTACT:".length());

                    Button contactButton = new Button(contactName);
                    contactButton.setMaxWidth(Double.MAX_VALUE); // Makes it expand
                    contactButton.getStyleClass().add("contact-button");
                    contactButton.setOnAction(e -> openChat(contactName));
                    contactsContainer.getChildren().add(contactButton);
                }
            }

            //clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void addContact(Event event) {
        try {
            AddContactAlert inputAlert = new AddContactAlert("Add Contact", "Enter the username of the contact to add:");
            Optional<String> result = inputAlert.showAndWait();

            if (result.isPresent()) {
                String contactToAdd = result.get();

                if (!contactToAdd.isEmpty() && !contactToAdd.equals(currentUsername)) {
                    ClientSocket clientSocket = Session.getClientSocket();

                    clientSocket.sendCommand("ADD_CONTACT:" + currentUsername + ":" + contactToAdd);

                    String response = clientSocket.readLine();

                    if (response.startsWith("ADD_CONTACT_SUCCESS")) {
                        new CustomAlert("Contact added successfully!");
                        loadContactsFromServer();
                    } else if (response.startsWith("ADD_CONTACT_FAIL")) {
                        String[] parts = response.split(":", 3);
                        String failedUser = parts.length > 1 ? parts[1] : "?";
                        String reason = parts.length > 2 ? parts[2] : "Unknown error";
                        new CustomAlert("Failed to add contact \"" + failedUser + "\": " + reason);
                    }
                } else {
                    new CustomAlert("Invalid username entered.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            new CustomAlert("Error occurred while adding contact.");
        }
    }
    
    @FXML 
    public void logOut(ActionEvent event) {
        try {
            ClientSocket clientSocket = Session.getClientSocket();
            if (clientSocket != null) {
                clientSocket.close(); // Close the old socket
            }
            Session.setClientSocket(null);        // Clear client socket
            Session.setCurrentUsername(null);     // Clear username
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/login.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            currentStage.setScene(scene);
            currentStage.setTitle("Login");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void openChat(String contactUsername) {
        System.out.println("Opening chat with: " + contactUsername + " (ContactsController)");
        try {
            // 1. Get client socket from session
            ClientSocket clientSocket = Session.getClientSocket();

            // 2. Identify user
            clientSocket.sendUsername(currentUsername);

            // 3. Send GET_CHAT request to server
            clientSocket.sendCommand("GET_CHAT:" + currentUsername + ":" + contactUsername);

            // 4. Read chat messages from server
            List<String> chatMessages = new ArrayList<>();
            String response;
            while (!(response = clientSocket.readLine()).equals("CHAT_DONE")) {
                if (response.startsWith("MESSAGE:")) {
                    String message = response.substring("MESSAGE:".length());
                    chatMessages.add(message);
                }
            }

            // 5. Load chat.fxml and switch scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/chat.fxml"));
            Parent chatPageRoot = loader.load();

            // 6. Set data in ChatController
            ChatController chatController = loader.getController();
            String currentUser = Session.getCurrentUsername();
            chatController.initializeChat(currentUser, contactUsername);

            // 7. Switch to chat scene
            Stage currentStage = (Stage) contactsContainer.getScene().getWindow();
            
            Scene scene = new Scene(chatPageRoot);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            currentStage.setScene(scene);
            currentStage.setTitle("Chat with " + contactUsername);
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  
}
