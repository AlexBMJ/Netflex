package pages;


import com.sun.javafx.scene.layout.region.Margins;
import javafx.collections.ObservableList;
import javafx.css.Stylesheet;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class BrowsePage implements Page {
    private Scene scene;

    public BrowsePage() {
        final VBox root = new VBox();
        root.setFillWidth(true);
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: rgb(30,30,30);");

        // Header
        final GridPane gp1 = new GridPane();
        final ImageView menuButton = new ImageView("menu.png");
        final ImageView logo = new ImageView("netflex_logo.png");
        menuButton.setPreserveRatio(true);
        menuButton.setFitHeight(40);
        logo.setPreserveRatio(true);
        logo.setFitHeight(40);
        GridPane.setMargin(menuButton, new Insets(5,15,5,15));
        GridPane.setMargin(logo, new Insets(5));
        gp1.setPrefHeight(50);
        gp1.setMinHeight(50);
        gp1.setMaxHeight(50);
        gp1.setStyle("-fx-background-color: black;");
        gp1.add(menuButton,0,0);
        gp1.add(logo,1,0);
        root.getChildren().add(gp1);

        // Base
        final StackPane stackPane = new StackPane();
        final AnchorPane mainAnchorPane = new AnchorPane();
        stackPane.setAlignment(Pos.TOP_CENTER);
        mainAnchorPane.setTopAnchor(stackPane, 0d);
        mainAnchorPane.setBottomAnchor(stackPane, 0d);
        mainAnchorPane.setLeftAnchor(stackPane, 0d);
        mainAnchorPane.setRightAnchor(stackPane, 0d);
        mainAnchorPane.getChildren().add(stackPane);
        root.getChildren().add(mainAnchorPane);

        // Scroll Container
        final ScrollPane scrollPane = new ScrollPane();
        final AnchorPane ap1 = new AnchorPane();
        final FlowPane flowPane = new FlowPane();
        flowPane.setPadding(new Insets(20));
        flowPane.setHgap(10);
        flowPane.setVgap(10);
        flowPane.setColumnHalignment(HPos.CENTER);
        flowPane.setAlignment(Pos.TOP_CENTER);
        flowPane.setStyle("-fx-background-color: rgb(30,30,30);");
        FlowPane.setMargin(scrollPane, new Insets(20));
        scrollPane.setContent(ap1);
        scrollPane.setStyle("-fx-background-color: rgb(30,30,30);");
        scrollPane.getStyleClass().add("scrollPane");
        scrollPane.getStylesheets().add("scrollbar.css");
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        AnchorPane.setTopAnchor(flowPane, 0d);
        AnchorPane.setBottomAnchor(flowPane, 0d);
        AnchorPane.setLeftAnchor(flowPane, 0d);
        AnchorPane.setRightAnchor(flowPane, 0d);
        ap1.getChildren().add(flowPane);
        stackPane.getChildren().add(scrollPane);

        for (int i=0; i<10; i++)
            flowPane.getChildren().add(new ImageView("icon.png"));

        // Search Field
        final TextField searchField = new TextField();
        searchField.setMaxWidth(300);
        searchField.setMaxHeight(25);
        searchField.setPromptText("Search");
        searchField.setStyle("-fx-background-color: rgb(20,20,20); -fx-text-fill: white");
        searchField.setOpacity(0.8f);
        searchField.setAlignment(Pos.TOP_CENTER);
        StackPane.setMargin(searchField, new Insets(15,0,0,0));
        stackPane.getChildren().add(searchField);

        // Filter Menu
        final HBox leftSplit = new HBox();
        final VBox menu = new VBox();
        leftSplit.setMaxWidth(Region.USE_COMPUTED_SIZE);
        leftSplit.setFillHeight(true);
        leftSplit.setStyle("-fx-background-color: rgba(0,0,0,0.1)");
        menu.setFillWidth(true);
        menu.setPadding(new Insets(0,0,0,20));
        menu.setStyle("-fx-background-color: black");
        menu.setSpacing(10);
        menu.setMaxWidth(200);
        menu.setMinWidth(200);
        menu.setAlignment(Pos.TOP_LEFT);
        for (int i=0; i<10; i++) {
            Label lbl = new Label(String.format("Item %s", i));
            lbl.setTextFill(Color.WHITE);
            lbl.setFont(new Font("Segoe UI Semibold", 24));
            menu.getChildren().add(lbl);
        }
        leftSplit.getChildren().add(menu);
        stackPane.getChildren().add(leftSplit);

        // Loading Image
        final ImageView loadingIcon = new ImageView("loading_medium.gif");

        scene = new Scene(root);
    }

    public Scene getScene() {
        return scene;
    }
}
