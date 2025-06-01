package com.sandugrecu.alertBoxes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomAlert implements AlertBox {

    private final String message;
    private Stage dialog;
    private VBox layout;

    public CustomAlert(String message) {
        this.message = message;
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

        Button closeButton = new Button("OK");
        closeButton.setOnAction(e -> dialog.close());

        layout = new VBox(10, label, closeButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1px;");
        layout.setPrefWidth(200);
    }

    @Override
    public void showScene() {
        if (dialog == null || layout == null) {
            buildScene(); 
        }
        
        Scene scene = new Scene(layout);

        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
