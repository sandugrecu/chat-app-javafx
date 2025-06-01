package com.sandugrecu.controllers;

import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import com.sandugrecu.client.ClientSocket;
import com.sandugrecu.client.Config;
import com.sandugrecu.alertBoxes.CustomAlert;
import com.sandugrecu.client.Session;

public class CreateAcountController {
	private String serverIP = Config.getServerIP();
	
	//facem legatura cu text field si password field prin fxid
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatedPasswordField;
    
    @FXML
    public void createAcount(ActionEvent event) {
    	// citim datele din createAcount.fxml
		String username = usernameField.getText();
		String password = passwordField.getText();
		String repeatedPassword = repeatedPasswordField.getText();
		
		try {
            // Create the socket connection
            ClientSocket clientSocket = new ClientSocket(serverIP, 12345);
            System.out.println("[INFO] Socket conection succesfull. (CreateAcountController)");
            
            // Identify the client to the server
            clientSocket.sendUsername(username);
            
            //Update the session 
            Session.setClientSocket(clientSocket);
            Session.setCurrentUsername(username);
            
            // Send REGISTER command
            clientSocket.sendCommand("REGISTER:" + username + ":" + password + ":" + repeatedPassword);
            
            // Read the response from the server
            String response = clientSocket.readLine();

            if (response.equals("REGISTER_SUCCESS")) {
                // Register successful, proceed to the next screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/contacts.fxml"));
                Parent root = loader.load();

                ContactsController contactsController = loader.getController();
                contactsController.setUsername(username);

                Stage currentStage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(root);
        		scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                
                currentStage.setScene(scene);
                currentStage.setTitle("Contacts");
                currentStage.show();
            } else if (response.startsWith("REGISTER_FAIL")){
            	String[] parts = response.split(":", 2);
            	//Citim mesajul erorii
            	String errorMessage = (parts.length > 1) ? parts[1] : "A aparut o eroare necunoscuta la inregistrare!";
            	
            	//Creem o alerta care contine mesajul cu eroarea care s-a comis
                new CustomAlert(errorMessage);
            }

            //clientSocket.close(); // Close the socket connection

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML 
    public void login(ActionEvent event) {
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/login.fxml"));
    		Parent root = loader.load();

    		// Creează scena și adaugă CSS-ul
    		Scene scene = new Scene(root);
    		scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

    		// Obține Stage-ul curent din eveniment
    		Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

    		// Setează scena și afișează
    		currentStage.setScene(scene);
    		currentStage.setTitle("Login");
    		currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void initialize() {
        // Run after UI is ready to request focus
        Platform.runLater(() -> usernameField.requestFocus());
    }
    
    @FXML
    public void handleEnterKey(javafx.scene.input.KeyEvent event) {
        switch (event.getCode()) {
            case ENTER -> login(null);  // Call login, event param not needed here
        }
    }
}
