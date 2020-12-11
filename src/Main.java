import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import pages.BrowsePage;
import pages.PageHandler;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.getIcons().add(new Image("icon.png"));
        PageHandler window = PageHandler.create(stage, 10);
        window.addPage(new BrowsePage());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
