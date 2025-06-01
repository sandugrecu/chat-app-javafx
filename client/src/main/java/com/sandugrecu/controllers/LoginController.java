package com.sandugrecu.controllers;

import java.io.IOException;

import java.util.Objects;

import com.sandugrecu.client.ClientSocket;
import com.sandugrecu.client.Config;
import com.sandugrecu.alertBoxes.CustomAlert;
import com.sandugrecu.alertBoxes.GetServerIPAlert;
import com.sandugrecu.client.Session;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private String serverIP;

    public void login(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();	

        try {
        	new GetServerIPAlert();
            serverIP = Config.getServerIP();
        	
        	// Check if the IP is valid
            if (!isValidIP(serverIP)) {
                new CustomAlert("Servers IP in not valid!");
                return;
            }
            
            if (!isServerReachable(serverIP)) {
                new CustomAlert("The server is not accessible.");
                return;
            }
            
            ClientSocket clientSocket = new ClientSocket(serverIP, 12345);        	
            System.out.println("[INFO] Socket connection successful. (LoginController)");
            
            // Identify the client to the server
            clientSocket.sendUsername(username);
            
            //Update the session 
            Session.setClientSocket(clientSocket);
            Session.setCurrentUsername(username);
            
            // Send LOGIN command
            clientSocket.sendCommand("LOGIN:" + username + ":" + password);
            
            // Read the response from the server
            String response = clientSocket.readLine();

            if (response.equals("LOGIN_SUCCESS")) {
                // Login successful, proceed to the next screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/contacts.fxml"));
                Parent root = loader.load();

                ContactsController contactsController = loader.getController();
                contactsController.setUsername(username);

                Stage currentStage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
                
                currentStage.setScene(scene);

                currentStage.setTitle("Contacts");
                currentStage.show();
            } else {
                System.out.println("Login Failed.");
                new CustomAlert("Login failed because...");
            }

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    @FXML
    public void createAccount(ActionEvent event) {
    	try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/createAccount.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());

            currentStage.setScene(scene);
            currentStage.setTitle("Create Account");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
    
    private boolean isValidIP(String ip) {
    	if (ip.equals("localhost"))
    		return true;
    	
    	//regex for a valid IP
        String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(regex);
    }
    
    private boolean isServerReachable(String ip) {
        try {
            // Create a ProcessBuilder for the ping command
            ProcessBuilder processBuilder = new ProcessBuilder("ping", "-n", "1", ip);  
            Process process = processBuilder.start();
            
            // Wait for the command to complete and get the exit value
            int returnVal = process.waitFor();
            
            // Return true if the server is reachable (exit value 0 means success)
            return returnVal == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.err);
            return false;
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
