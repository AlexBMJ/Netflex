package pages;


import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class BrowsePage implements Page {
    final private String[] genres = {"Crime", "Drama", "Biography", "History", "Sport", "Romance", "War", "Mystery", "Adventure", "Family", "Fantasy", "Thriller", "Horror", "Film-Noir", "Musical", "Sci-fi", "Comedy", "Action", "Western"};

    private Scene scene;
    private Boolean menuHover = false;
    private String searchSource;
    private String genreFilter;
    private String yearFilter;
    private String scoreFilter;

    FadeTransition fadeOutTransition = new FadeTransition();
    TranslateTransition closeMenuTransition = new TranslateTransition();

    final VBox menuList = new VBox();

    public BrowsePage() {
        final VBox root = new VBox();
        root.setFillWidth(true);
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: rgb(30,30,30);");

        // Header
        final GridPane gp1 = new GridPane();
        final ImageView menuButton = new ImageView("menu.png");
        final Pane menuButtonPane = new Pane(menuButton);
        final ImageView logo = new ImageView("netflex_logo.png");
        menuButton.setPreserveRatio(true);
        menuButton.setFitHeight(40);
        menuButtonPane.setCursor(Cursor.HAND);
        logo.setPreserveRatio(true);
        logo.setFitHeight(40);
        GridPane.setMargin(menuButtonPane, new Insets(5,15,5,15));
        GridPane.setMargin(logo, new Insets(5));
        gp1.setPrefHeight(50);
        gp1.setMinHeight(50);
        gp1.setMaxHeight(50);
        gp1.setStyle("-fx-background-color: black;");
        gp1.add(menuButtonPane,0,0);
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
        final FlowPane flowPane = new FlowPane();
        flowPane.setPadding(new Insets(20));
        flowPane.setHgap(10);
        flowPane.setVgap(10);
        flowPane.setColumnHalignment(HPos.CENTER);
        flowPane.setAlignment(Pos.TOP_CENTER);
        flowPane.setStyle("-fx-background-color: rgb(30,30,30);");
        FlowPane.setMargin(scrollPane, new Insets(20));
        scrollPane.setContent(flowPane);
        scrollPane.setStyle("-fx-background-color: rgb(30,30,30);");
        scrollPane.getStyleClass().add("scrollPane");
        scrollPane.getStylesheets().add("scrollbar.css");
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        stackPane.getChildren().add(scrollPane);

        // TESTING
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
        final ScrollPane menu = new ScrollPane();
        final AnchorPane menuAnchorPane = new AnchorPane();
        leftSplit.setMaxWidth(Region.USE_COMPUTED_SIZE);
        leftSplit.setFillHeight(true);
        leftSplit.setStyle("-fx-background-color: rgba(0,0,0,0.5)");
        menu.setFitToHeight(true);
        menu.setFitToWidth(true);
        menu.setMaxWidth(250);
        menu.setMinWidth(250);
        menu.getStyleClass().add("scrollPane");
        menu.getStylesheets().add("scrollbar.css");
        menu.setStyle("-fx-background-color: black");
        menuAnchorPane.setStyle("-fx-background-color: black");
        menuList.setStyle("-fx-background-color: black");
        menuList.setPadding(new Insets(0,0,0,20));
        menuList.setSpacing(10);
        menuList.setFillWidth(true);
        menuList.setAlignment(Pos.TOP_LEFT);

        AnchorPane.setTopAnchor(menuList, 0d);
        AnchorPane.setBottomAnchor(menuList, 0d);
        AnchorPane.setLeftAnchor(menuList, 0d);
        AnchorPane.setRightAnchor(menuList, 0d);

        TranslateTransition openMenuTransition = new TranslateTransition();
        openMenuTransition.setToX(0);
        openMenuTransition.setDuration(Duration.millis(500));
        openMenuTransition.setAutoReverse(false);
        openMenuTransition.setNode(menu);

        closeMenuTransition.setToX(-250);
        closeMenuTransition.setDuration(Duration.millis(500));
        closeMenuTransition.setAutoReverse(false);
        closeMenuTransition.setOnFinished(actionEvent -> menu.setVisible(false));
        closeMenuTransition.setNode(menu);

        FadeTransition fadeInTransition = new FadeTransition();
        fadeInTransition.setToValue(1);
        fadeInTransition.setDuration(Duration.millis(250));
        fadeInTransition.setAutoReverse(false);
        fadeInTransition.setNode(leftSplit);

        fadeOutTransition.setToValue(0);
        fadeOutTransition.setDuration(Duration.millis(250));
        fadeOutTransition.setDelay(Duration.millis(400));
        fadeOutTransition.setAutoReverse(false);
        fadeOutTransition.setOnFinished(actionEvent -> {
            leftSplit.setVisible(false);
        });
        fadeOutTransition.setNode(leftSplit);

        menuButtonPane.setOnMouseClicked(mouseEvent -> {
            if (leftSplit.isVisible()) {
                closeMenuTransition.play();
                fadeOutTransition.play();
            } else {
                mainMenuItems();
                leftSplit.setVisible(true);
                menu.setVisible(true);
                openMenuTransition.play();
                fadeInTransition.play();
            }

        });

        menu.setOnMouseEntered(mouseEvent -> menuHover = true);
        menu.setOnMouseExited(mouseEvent -> menuHover = false);
        leftSplit.setOnMouseClicked(mouseEvent -> {if (!menuHover) closeMenu();});

        mainMenuItems();
        menuAnchorPane.getChildren().add(menuList);
        menu.setContent(menuAnchorPane);
        leftSplit.getChildren().add(menu);
        stackPane.getChildren().add(leftSplit);


        // Loading Image
        final ImageView loadingIcon = new ImageView("loading_medium.gif");

        scene = new Scene(root);
    }

    private void closeMenu() {
        closeMenuTransition.play();
        fadeOutTransition.play();
    }

    private void mainMenuItems() {
        menuList.getChildren().clear();
        Label filterTitleLbl = new Label("Filters");
        filterTitleLbl.setTextFill(Color.GRAY);
        filterTitleLbl.setFont(new Font("Segoe UI Semibold Italic", 20));
        menuList.getChildren().add(filterTitleLbl);

        Label genreLabel = new Label("Genre");
        genreLabel.setCursor(Cursor.HAND);
        genreLabel.setTextFill(Color.WHITE);
        genreLabel.setFont(new Font("Segoe UI Thin", 24));
        genreLabel.setOnMouseClicked(mouseEvent -> genreFilterUI());
        Label yearLabel = new Label("Year");
        yearLabel.setCursor(Cursor.HAND);
        yearLabel.setTextFill(Color.WHITE);
        yearLabel.setFont(new Font("Segoe UI Thin", 24));
        yearLabel.setOnMouseClicked(mouseEvent -> yearFilterUI());
        Label scoreLabel = new Label("Score");
        scoreLabel.setCursor(Cursor.HAND);
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(new Font("Segoe UI Thin", 24));
        scoreLabel.setOnMouseClicked(mouseEvent -> scoreFilterUI());
        Label clearLabel = new Label("Clear Filters");
        clearLabel.setCursor(Cursor.HAND);
        clearLabel.setTextFill(Color.DARKRED);
        clearLabel.setFont(new Font("Segoe UI Thin", 20));
        clearLabel.setOnMouseClicked(mouseEvent -> {genreFilter=null;yearFilter=null;scoreFilter=null;searchSource=null;closeMenu();});

        menuList.getChildren().add(genreLabel);
        menuList.getChildren().add(yearLabel);
        menuList.getChildren().add(scoreLabel);
        menuList.getChildren().add(clearLabel);
    }

    private void yearFilterUI() {
        menuList.getChildren().clear();
        Label filterTitleLbl = new Label("\uE0BA Filter By Year");
        filterTitleLbl.setCursor(Cursor.HAND);
        filterTitleLbl.setTextFill(Color.GRAY);
        filterTitleLbl.setFont(new Font("Segoe UI Semibold Italic", 20));
        filterTitleLbl.setOnMouseClicked(mouseEvent -> mainMenuItems());
        menuList.getChildren().add(filterTitleLbl);
        for (int i=2; i<=12; i++) {
            Label lbl = new Label(String.format("%s", 1900 + i*10));
            lbl.setCursor(Cursor.HAND);
            lbl.setTextFill(Color.WHITE);
            lbl.setFont(new Font("Segoe UI Thin", 24));
            int finalI = i;
            lbl.setOnMouseClicked(mouseEvent -> {yearFilter=String.valueOf(1900+ finalI*10);closeMenu();});
            menuList.getChildren().add(lbl);
        }
    }

    private void genreFilterUI() {
        menuList.getChildren().clear();
        Label filterTitleLbl = new Label("\uE0BA Filter By Genre");
        filterTitleLbl.setCursor(Cursor.HAND);
        filterTitleLbl.setTextFill(Color.GRAY);
        filterTitleLbl.setFont(new Font("Segoe UI Semibold Italic", 20));
        filterTitleLbl.setOnMouseClicked(mouseEvent -> mainMenuItems());
        menuList.getChildren().add(filterTitleLbl);
        for (String g : genres) {
            Label lbl = new Label(g);
            lbl.setCursor(Cursor.HAND);
            lbl.setTextFill(Color.WHITE);
            lbl.setFont(new Font("Segoe UI Thin", 24));
            menuList.getChildren().add(lbl);
        }
    }

    private void scoreFilterUI() {
        menuList.getChildren().clear();
        Label filterTitleLbl = new Label("\uE0BA Filter By Score");
        filterTitleLbl.setCursor(Cursor.HAND);
        filterTitleLbl.setTextFill(Color.GRAY);
        filterTitleLbl.setFont(new Font("Segoe UI Semibold Italic", 20));
        filterTitleLbl.setOnMouseClicked(mouseEvent -> mainMenuItems());
        menuList.getChildren().add(filterTitleLbl);
        for (int i=1; i<=10; i++) {
            Label lbl = new Label(String.format("[ %s ]", i));
            lbl.setCursor(Cursor.HAND);
            lbl.setTextFill(Color.WHITE);
            lbl.setFont(new Font("Segoe UI Thin", 24));
            int finalI = i;
            lbl.setOnMouseClicked(mouseEvent -> {scoreFilter=(String.valueOf(finalI));closeMenu();});
            menuList.getChildren().add(lbl);
        }
    }

    public Scene getScene() {
        return scene;
    }
}
