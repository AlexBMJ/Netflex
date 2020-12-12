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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BrowsePage implements Page {
    final private String[] genres = {"Crime", "Drama", "Biography", "History", "Sport", "Romance", "War", "Mystery", "Adventure", "Family", "Fantasy", "Thriller", "Horror", "Film-Noir", "Musical", "Sci-fi", "Comedy", "Action", "Western"};

    private Scene scene;
    private Boolean menuHover = false;
    private String searchSource = "local";
    private boolean movieSearch;
    private HashMap<String, String> searchFilters = new HashMap();

    FadeTransition fadeOutTransition = new FadeTransition();
    TranslateTransition closeMenuTransition = new TranslateTransition();

    final VBox menuList = new VBox();
    final Label searchFilterLabel = new Label("");

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
        GridPane.setMargin(logo, new Insets(0,0,0,70));
        GridPane.setMargin(searchFilterLabel, new Insets(5,25,5,0));
        searchFilterLabel.setAlignment(Pos.CENTER_RIGHT);
        searchFilterLabel.setTextFill(Color.WHITE);
        searchFilterLabel.setFont(new Font("Segoe UI Bold", 20));
        gp1.setPrefHeight(50);
        gp1.setMinHeight(50);
        gp1.setMaxHeight(50);
        ColumnConstraints cc1 = new ColumnConstraints();
        cc1.setHgrow(Priority.ALWAYS);
        cc1.setHalignment(HPos.LEFT);
        ColumnConstraints cc2 = new ColumnConstraints();
        cc2.setHgrow(Priority.ALWAYS);
        cc2.setHalignment(HPos.RIGHT);
        gp1.setStyle("-fx-background-color: black;");
        gp1.getColumnConstraints().add(0, cc1);
        gp1.getColumnConstraints().add(1, cc2);
        gp1.add(menuButtonPane,0,0);
        gp1.add(logo,0,0);
        gp1.add(searchFilterLabel, 1, 0);
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
        scrollPane.setContent(flowPane);
        scrollPane.setStyle("-fx-background-color: rgb(30,30,30);");
        scrollPane.getStyleClass().add("scrollPane");
        scrollPane.getStylesheets().add("scrollbar.css");
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        stackPane.getChildren().add(scrollPane);

        // TESTING
        for (int i=0; i<20; i++)
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
        fadeOutTransition.setOnFinished(actionEvent -> leftSplit.setVisible(false));
        fadeOutTransition.setNode(leftSplit);

        menuButtonPane.setOnMouseClicked(mouseEvent -> {
            if (leftSplit.isVisible()) {
                closeMenu();
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
        leftSplit.setVisible(false);
        closeMenu();


        // Loading Image
        final ImageView loadingIcon = new ImageView("loading_medium.gif");

        scene = new Scene(root);
    }

    private void initSearchFilters() {
        if (searchSource == "external")
            searchFilterLabel.setText("API Search");
        else if (searchSource == "local") {
            if (searchFilters.size() > 0) {
                searchFilterLabel.setText(String.format("%s: %s", (movieSearch ? "Movies" : "TV Shows"), Arrays.asList(searchFilters.values().toArray()).toString().replaceAll("(\\[)|(\\])", "")));
            } else
                searchFilterLabel.setText((movieSearch ? "Movies" : "TV Shows"));
        }
    }

    private void closeMenu() {
        closeMenuTransition.play();
        fadeOutTransition.play();
        initSearchFilters();
    }

    private void mainMenuItems() {
        menuList.getChildren().clear();
        Label filterTitleLbl = new Label("Filters");
        filterTitleLbl.setTextFill(Color.GRAY);
        filterTitleLbl.setFont(new Font("Segoe UI Semibold Italic", 20));
        menuList.getChildren().add(filterTitleLbl);

        Label movieTVshowLabel = new Label((movieSearch ? "TV Shows" : "Movies"));
        movieTVshowLabel.setCursor(Cursor.HAND);
        movieTVshowLabel.setTextFill(Color.WHITE);
        movieTVshowLabel.setFont(new Font("Segoe UI Thin", 24));
        movieTVshowLabel.setOnMouseClicked(mouseEvent -> {movieSearch=!movieSearch;closeMenu();});
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
        Label APIsearchLabel = new Label("API Search");
        APIsearchLabel.setCursor(Cursor.HAND);
        APIsearchLabel.setTextFill(Color.WHITE);
        APIsearchLabel.setFont(new Font("Segoe UI Thin", 24));
        APIsearchLabel.setOnMouseClicked(mouseEvent -> {searchFilters.clear();searchSource="external";closeMenu();});
        Label clearLabel = new Label("Clear Filters");
        clearLabel.setCursor(Cursor.HAND);
        clearLabel.setTextFill(Color.web("#E08000"));
        clearLabel.setFont(new Font("Segoe UI Thin", 20));
        clearLabel.setOnMouseClicked(mouseEvent -> {searchFilters.clear();searchSource="local";closeMenu();});

        menuList.getChildren().add(movieTVshowLabel);
        menuList.getChildren().add(genreLabel);
        menuList.getChildren().add(yearLabel);
        menuList.getChildren().add(scoreLabel);
        menuList.getChildren().add(APIsearchLabel);
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
            Label lbl = new Label(String.format("%s", 1900+i*10));
            lbl.setCursor(Cursor.HAND);
            lbl.setTextFill(Color.WHITE);
            lbl.setFont(new Font("Segoe UI Thin", 24));
            int finalI = i;
            lbl.setOnMouseClicked(mouseEvent -> {searchFilters.put("year",String.valueOf(1900+finalI*10));closeMenu();});
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
            lbl.setOnMouseClicked(mouseEvent -> {searchFilters.put("genre",g);closeMenu();});
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
            Label lbl = new Label(String.format("[%s / 10]", i));
            lbl.setCursor(Cursor.HAND);
            lbl.setTextFill(Color.WHITE);
            lbl.setFont(new Font("Segoe UI Thin", 24));
            int finalI = i;
            lbl.setOnMouseClicked(mouseEvent -> {searchFilters.put("score",String.valueOf(finalI));closeMenu();});
            menuList.getChildren().add(lbl);
        }
    }

    public Scene getScene() {
        return scene;
    }
}
