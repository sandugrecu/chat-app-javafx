package com.sandugrecu.alertBoxes;

import com.sandugrecu.client.Config;
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

public class GetServerIPAlert implements AlertBox {

    private final String message;
    private Stage dialog;
    private VBox layout;
    private TextField inputField;
    private String inputResult;

    public GetServerIPAlert() {
        message = "Care e adresa ip a serverului: ";
        this.buildScene();
        this.showScene();
    }

    @Override
    public void buildScene() {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        Label label = new Label(message);
        label.setWrapText(true);

        inputField = new TextField();
        inputField.setText("localhost");
        inputField.setPromptText("Enter something...");

        Button connectButton = new Button("Connect");
        connectButton.setOnAction(e -> {
            inputResult = inputField.getText();
            Config.setServerIP(inputResult);
            dialog.close();
        });
        connectButton.getStyleClass().add("custom-alert-button");

        inputField.setOnAction(e -> connectButton.fire());

        layout = new VBox(10, label, inputField, connectButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setPrefWidth(200);

        // Apply CSS class instead of inline style
        layout.getStyleClass().add("custom-alert");
    }

    @Override
    public void showScene() {
        if (dialog == null || layout == null) {
            buildScene();
        }

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
