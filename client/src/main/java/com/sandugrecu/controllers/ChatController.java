package com.sandugrecu.controllers;

import java.io.IOException;
import java.util.Objects;

import com.sandugrecu.client.ClientSocket;
import com.sandugrecu.client.Session;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ChatController {

    @FXML
    private ListView<HBox> chatListView;

    @FXML
    private TextField messageInput;

    private String contactName;
    private String currentUser;

    private volatile boolean listening = true;
    private Thread listenerThread;

    public void initializeChat(String currentUser, String contactName) {
        this.currentUser = currentUser;
        this.contactName = contactName;
        
        // First, request chat history from server
        try {
            ClientSocket clientSocket = Session.getClientSocket();
            clientSocket.sendCommand("GET_CHAT:" + currentUser + ":" + contactName);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        // Then start listening for new messages pushed by server
        startListeningForMessages();
        
        // Add global key listener after scene is ready
        Platform.runLater(() -> {
            messageInput.getScene().setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case SLASH -> {
                        messageInput.requestFocus();
                        messageInput.selectAll();
                    }
                    case ESCAPE -> {
                    	this.backToContacts();
                    }
                }
            });
        });
    }

    private void startListeningForMessages() {
        listening = true;

        listenerThread = new Thread(() -> {
            try {
                ClientSocket clientSocket = Session.getClientSocket();

                // Notify server to start pushing new messages for this chat
                clientSocket.sendCommand("START_LISTEN:" + currentUser + ":" + contactName);

                while (listening) {
                    String line = clientSocket.readLine();

                    if (line == null) break;

                    if (line.startsWith("MESSAGE:")) {
                        // This covers both history and live messages
                        String rawMessage = line.substring(8);
                        int index = rawMessage.indexOf(": ");
                        if (index > 0) {
                            String sender = rawMessage.substring(0, index);
                            String content = rawMessage.substring(index + 2);

                            Platform.runLater(() -> addMessage(sender, content));
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    @FXML
    public void sendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            messageInput.clear();
            try {
                ClientSocket clientSocket = Session.getClientSocket();
                clientSocket.sendCommand("SEND_MESSAGE:" + currentUser + ":" + contactName + ":" + message);
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    @FXML
    public void handleEnterKey(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER -> sendMessage();
        }

    }

    private void addMessage(String sender, String content) {
        Label messageLabel = new Label(content);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        HBox messageBox = new HBox(messageLabel);
        messageBox.setFillHeight(true);

        if (sender.equals(currentUser)) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageLabel.setStyle("-fx-background-color: #DCF8C6; -fx-padding: 10; -fx-background-radius: 10;");
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            messageLabel.setStyle("-fx-background-color: #F1F1F1; -fx-padding: 10; -fx-background-radius: 10;");
        }

        chatListView.getItems().add(messageBox);
        chatListView.scrollTo(chatListView.getItems().size() - 1);
        chatListView.setSelectionModel(null);
    }

    public void stopListening() {
        listening = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }
    

	@FXML
	public void backToContacts(ActionEvent event) {
		this.stopListening();
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/contacts.fxml"));
			Parent root = loader.load();
			
			ContactsController contactsController = loader.getController();
			contactsController.setUsername(Session.getCurrentUsername());
			
			Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
			
			currentStage.setScene(scene);
			currentStage.setTitle("Contacts");
			currentStage.show();
			
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public void backToContacts() {
		this.stopListening();
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/contacts.fxml"));
			Parent root = loader.load();
			
			ContactsController contactsController = loader.getController();
			contactsController.setUsername(Session.getCurrentUsername());
			
	        Stage stage = (Stage) chatListView.getScene().getWindow();
	        Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
			
	        stage.setScene(scene);
	        stage.setTitle("Contacts");
	        stage.show();
			
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}
}
