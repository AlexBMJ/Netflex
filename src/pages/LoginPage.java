package pages;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class LoginPage implements Page {
    private Scene scene;

    public LoginPage() {
        Button backButton = new Button("<<< Back");
        Button switchPage = new Button("Browse Page");
        Text text = new Text();
        text.setFont(new Font(45));
        text.setX(50);
        text.setY(150);
        text.setText("LOGIN PAGE");

        switchPage.setOnMouseClicked(mouseEvent -> {
            PageHandler.getInstance().addPage(new BrowsePage());
        });

        backButton.setOnMouseClicked(mouseEvent -> {
            try {
                PageHandler.getInstance().prevPage();
            } catch (PageCacheException e) {
                System.out.println(e.getMessage());
            }
        });

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.TOP_LEFT);

        Group root = new Group();
        root.getChildren().add(vbox);
        ObservableList list = vbox.getChildren();
        list.add(backButton);
        list.add(switchPage);
        list.add(text);
        TextField tf = new TextField();
        list.add(tf);
        scene = new Scene(root, 600, 300);
        tf.requestFocus();
    }

    public Scene getScene() {
        return scene;
    }
}
