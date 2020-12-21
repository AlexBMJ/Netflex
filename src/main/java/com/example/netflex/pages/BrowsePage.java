package com.example.netflex.pages;


import com.example.netflex.content.Content;
import com.example.netflex.pages.components.CoverImage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;

public class BrowsePage implements Page {
    private Scene scene;

    final private VBox menuList = new VBox();
    final public FlowPane flowPane = new FlowPane();
    final public ScrollPane scrollPane = new ScrollPane();
    final public HBox menuBackground = new HBox();
    final public ScrollPane menu = new ScrollPane();
    final public TextField searchField = new TextField();
    final public Pane menuButtonPane = new Pane();
    final private Label searchFilterLabel = new Label("");
    final private ImageView loadingGif = new ImageView("loading_medium.gif");

    final public Label clearFiltersLabel = new Label("Clear Filters");

    final private TranslateTransition openMenuTransition = new TranslateTransition();
    final private FadeTransition fadeInTransition = new FadeTransition();
    final private FadeTransition fadeOutTransition = new FadeTransition();
    final private TranslateTransition closeMenuTransition = new TranslateTransition();

    public BrowsePage() {
        // Root
        final VBox root = new VBox();
        root.setFillWidth(true);
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: rgb(30,30,30);");

        // Header
        final GridPane gp1 = new GridPane();
        final ImageView menuButton = new ImageView("menu.png");
        final ImageView logo = new ImageView("netflex_logo.png");
        menuButtonPane.getChildren().add(menuButton);
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
        stackPane.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        root.getChildren().add(stackPane);

        // Scroll Container
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

        // Search Field
        searchField.setMaxWidth(300);
        searchField.setMaxHeight(25);
        searchField.setPromptText("Search");
        searchField.setFont(new Font("Segoe UI Bold", 20));
        searchField.setStyle("-fx-background-color: rgb(20,20,20); -fx-text-fill: white");
        searchField.setOpacity(0.9f);
        searchField.setAlignment(Pos.TOP_CENTER);
        StackPane.setMargin(searchField, new Insets(20,0,0,0));
        stackPane.getChildren().add(searchField);

        // Loading Image
        loadingGif.setBlendMode(BlendMode.SCREEN);
        loadingGif.setPreserveRatio(true);
        loadingGif.setFitWidth(400);
        loadingGif.setVisible(false);
        StackPane.setMargin(loadingGif, new Insets(0, 0, 20,0));
        StackPane.setAlignment(loadingGif, Pos.BOTTOM_CENTER);
        stackPane.getChildren().add(loadingGif);

        // Filter Menu
        final AnchorPane menuAnchorPane = new AnchorPane();
        menuBackground.setMaxWidth(Region.USE_COMPUTED_SIZE);
        menuBackground.setFillHeight(true);
        menuBackground.setStyle("-fx-background-color: rgba(0,0,0,0.5)");
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

        openMenuTransition.setToX(0);
        openMenuTransition.setDuration(Duration.millis(500));
        openMenuTransition.setAutoReverse(false);
        openMenuTransition.setNode(menu);

        closeMenuTransition.setToX(-250);
        closeMenuTransition.setDuration(Duration.millis(500));
        closeMenuTransition.setAutoReverse(false);
        closeMenuTransition.setOnFinished(actionEvent -> menu.setVisible(false));
        closeMenuTransition.setNode(menu);

        fadeInTransition.setToValue(1);
        fadeInTransition.setDuration(Duration.millis(250));
        fadeInTransition.setAutoReverse(false);
        fadeInTransition.setNode(menuBackground);

        fadeOutTransition.setToValue(0);
        fadeOutTransition.setDuration(Duration.millis(250));
        fadeOutTransition.setDelay(Duration.millis(400));
        fadeOutTransition.setAutoReverse(false);
        fadeOutTransition.setOnFinished(actionEvent -> menuBackground.setVisible(false));
        fadeOutTransition.setNode(menuBackground);

        menuAnchorPane.getChildren().add(menuList);
        menu.setContent(menuAnchorPane);
        menuBackground.getChildren().add(menu);
        stackPane.getChildren().add(menuBackground);
        menuBackground.setVisible(false);
        closeMenu();

        scene = new Scene(root);
    }

    public void clearFlowPane() {
        flowPane.getChildren().clear();
    }

    public void updateFilterLabel(String filter) {
        searchFilterLabel.setText(filter);
    }

    public void openMenu() {
        menuBackground.setVisible(true);
        menu.setVisible(true);
        openMenuTransition.play();
        fadeInTransition.play();
    }

    public void closeMenu() {
        closeMenuTransition.play();
        fadeOutTransition.play();
    }

    public void setMainMenuItems(EventHandler<MouseEvent> resetAction, LinkedHashMap<String, EventHandler<MouseEvent>> buttons) {
        menuList.getChildren().clear();
        Label filterTitleLbl = new Label("Filter Menu");
        filterTitleLbl.setTextFill(Color.GRAY);
        filterTitleLbl.setFont(new Font("Segoe UI Semibold Italic", 20));
        menuList.getChildren().add(filterTitleLbl);

        for (Map.Entry<String, EventHandler<MouseEvent>> button : buttons.entrySet()){
            Label menuLabel = new Label(button.getKey());
            menuLabel.setCursor(Cursor.HAND);
            menuLabel.setTextFill(Color.DARKGRAY);
            menuLabel.setFont(new Font("Segoe UI Thin", 24));
            menuLabel.setOnMouseClicked(button.getValue());
            menuLabel.setOnMouseEntered(mouseEvent -> menuLabel.setTextFill(Color.WHITE));
            menuLabel.setOnMouseExited(mouseEvent -> menuLabel.setTextFill(Color.DARKGRAY));
            menuList.getChildren().add(menuLabel);
        }

        clearFiltersLabel.setCursor(Cursor.HAND);
        clearFiltersLabel.setTextFill(Color.web("#E08000"));
        clearFiltersLabel.setFont(new Font("Segoe UI Thin", 20));
        clearFiltersLabel.setOnMouseClicked(resetAction);
        clearFiltersLabel.setOnMouseEntered(mouseEvent -> clearFiltersLabel.setTextFill(Color.ORANGE));
        clearFiltersLabel.setOnMouseExited(mouseEvent -> clearFiltersLabel.setTextFill(Color.web("#E08000")));
        menuList.getChildren().add(clearFiltersLabel);
    }

    public void setMenuItems(String menuTitle, EventHandler<MouseEvent> backAction, LinkedHashMap<String, EventHandler<MouseEvent>> buttons) {
        menuList.getChildren().clear();
        Label filterTitleLbl = new Label("\uE0BA Filter By "+menuTitle);
        filterTitleLbl.setCursor(Cursor.HAND);
        filterTitleLbl.setTextFill(Color.GRAY);
        filterTitleLbl.setFont(new Font("Segoe UI Semibold Italic", 20));
        filterTitleLbl.setOnMouseClicked(backAction);
        menuList.getChildren().add(filterTitleLbl);
        for (Map.Entry<String, EventHandler<MouseEvent>> button : buttons.entrySet()){
            Label lbl = new Label(button.getKey());
            lbl.setCursor(Cursor.HAND);
            lbl.setTextFill(Color.DARKGRAY);
            lbl.setFont(new Font("Segoe UI Thin", 24));
            lbl.setOnMouseEntered(mouseEvent -> lbl.setTextFill(Color.WHITE));
            lbl.setOnMouseExited(mouseEvent -> lbl.setTextFill(Color.DARKGRAY));
            lbl.setOnMouseClicked(button.getValue());
            menuList.getChildren().add(lbl);
        }
    }

    public void addCover(Content result, EventHandler<MouseEvent> clickEvent) {
        CoverImage imgView = new CoverImage(new Image("placeholder.jpg"), result, 0.65f);
        imgView.setPreserveRatio(true);
        imgView.setFitWidth(250);
        DropShadow shadow = new DropShadow();
        shadow.setBlurType(BlurType.ONE_PASS_BOX);
        imgView.setEffect(shadow);
        imgView.setCursor(Cursor.HAND);
        imgView.setSmooth(true);

        ScaleTransition scaleUpTransition = new ScaleTransition();
        scaleUpTransition.setDuration(Duration.millis(100));
        scaleUpTransition.setNode(imgView);
        scaleUpTransition.setToX(1.05);
        scaleUpTransition.setToY(1.05);

        ScaleTransition scaleDownTransition = new ScaleTransition();
        scaleDownTransition.setDuration(Duration.millis(100));
        scaleDownTransition.setNode(imgView);
        scaleDownTransition.setToX(1);
        scaleDownTransition.setToY(1);

        imgView.setOnMouseEntered(MouseEvent -> scaleUpTransition.play());
        imgView.setOnMouseExited(MouseEvent -> scaleDownTransition.play());
        imgView.setOnMouseClicked(clickEvent);

        flowPane.getChildren().add(imgView);
    }

    public void setNoResultsElement(String query, boolean movieSearch) {
        Label lbl = new Label(String.format("No results%s in %s", (query != null ? " for \""+query+"\"" : ""), (!movieSearch ? "TV Shows" : "Movies")));
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(new Font("Segoe UI Semibold", 24));
        FlowPane.setMargin(lbl, new Insets(50, 0, 0, 0));
        flowPane.getChildren().add(lbl);
    }

    public void setLoading(boolean v) {
        loadingGif.setVisible(v);
    }

    @Override
    public Scene getScene() {
        return scene;
    }
}
