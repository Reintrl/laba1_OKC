package org.example;

import org.example.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RunMain extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();

        primaryStage.setTitle("Serial Communication Module - RS-232/RS-485");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            controller.shutdown();
        });
    }

    public static void run(String[] args) {
        launch(args);
    }
}