package com.sandugrecu.alertBoxes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

public class AddContactAlert {

    private String title;
    private String message;
    private Stage dialog;
    private String userInput;

    public AddContactAlert(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public Optional<String> showAndWait() {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle(title);

        Label label = new Label(message);
        label.getStyleClass().add("custom-alert-label");

        TextField inputField = new TextField();
        inputField.setPromptText("Enter username...");
        inputField.setMaxWidth(200);

        Button okButton = new Button("OK");
        okButton.getStyleClass().add("custom-alert-button");
        okButton.setOnAction(e -> {
            userInput = inputField.getText().trim();
            dialog.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("custom-alert-button");
        cancelButton.setOnAction(e -> {
            userInput = null;
            dialog.close();
        });

        VBox layout = new VBox(10, label, inputField, okButton, cancelButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.getStyleClass().add("custom-alert");

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();

        return Optional.ofNullable(userInput).filter(input -> !input.isEmpty());
    }
}
