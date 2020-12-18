import content.LocalContent;
import content.MovieContent;
import database.Database;
import database.SearchDatabase;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import pages.BrowsePage;
import pages.MoviePage;
import pages.PageHandler;

import java.util.HashMap;

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
        PageHandler window = PageHandler.create(stage, 10);
        window.addPage(new BrowsePage());
    }

    public static void main(String[] args) {
        launch(args);
    }
}


