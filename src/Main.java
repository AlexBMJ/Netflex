import javafx.application.Application;
import javafx.stage.Stage;
import pages.BrowsePage;
import pages.PageHandler;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        PageHandler window = PageHandler.newPageHandler(stage, 5);
        window.addPage(new BrowsePage());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
