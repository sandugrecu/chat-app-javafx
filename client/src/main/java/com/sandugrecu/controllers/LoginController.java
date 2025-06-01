package com.sandugrecu.controllers;

import java.io.IOException;

import java.util.Optional;

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
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;


public class LoginController {

	//facem legatura cu text field si password field prin fxid
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;   
    
	//private String serverIP = Config.getServerIP();
    private String serverIP;
    
    public void login(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();	

        try {
        	//afisam un alert box si citim de la utilizator ip-ul serverului
        	new GetServerIPAlert();
        	
            // citim ip-ul serverului din Configurari
        	serverIP = Config.getServerIP();
        	
        	// Check if the IP is valid
            if (!isValidIP(serverIP)) {
                new CustomAlert("IP-ul serverului nu este valid.");
                return;
            }
            
            if (!isServerReachable(serverIP)) {
                new CustomAlert("Serverul nu este accesibil.");
                return;
            }
            
            ClientSocket clientSocket = new ClientSocket(serverIP, 12345);        	
            System.out.println("[INFO] Socket conection succesfull. (LoginController)");
            
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
                //cream o scena nou si aplicam css
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                
                currentStage.setScene(scene);

                currentStage.setTitle("Contacts");
                currentStage.show();
            } else {
                System.out.println("Login Failed.");
                
                //TO IMPLEMENT
                new CustomAlert("Login failed because...");
            }

            //clientSocket.close(); // Close the socket connection

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    @FXML
    public void createAcount(ActionEvent event) {
    	try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/createAcount.fxml"));
            Parent root = loader.load();

            // Obține Stage-ul curent din eveniment
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            
            // Schimbă scena
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            currentStage.setScene(scene);
            currentStage.setTitle("Create Account");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    }
    
    private boolean isValidIP(String ip) {
    	if (ip.equals("localhost"))
    		return true;
    	
    	//regex pentru un ip valid
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
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    public void initialize() {
        // Run after UI is ready to request focus
        Platform.runLater(() -> usernameField.requestFocus());
        
        //verificam daca gasesc fisierele
        //System.out.println(getClass().getResource("/css/styles.css"));
        //System.out.println(getClass().getResource("/fxmlFiles/login.fxml"));
    }
    
    @FXML
    public void handleEnterKey(javafx.scene.input.KeyEvent event) {
        switch (event.getCode()) {
            case ENTER -> login(null);  // Call login, event param not needed here
        }
    }

}
