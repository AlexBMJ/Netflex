package com.example.netflex;

import com.example.netflex.controllers.BrowseController;
import com.example.netflex.pages.StreamingService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.setTitle("Netflex");
        stage.getIcons().add(new Image("icon.png"));
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        StreamingService window = StreamingService.create(stage, 10);
        window.addPage(new BrowseController());
    }

    public static void main(String[] args) {
        launch(args);
    }
}