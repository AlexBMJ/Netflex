package pages;


import content.*;
import database.AlgoliaAPI;
import database.SearchDatabase;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import pages.components.CoverImage;

import java.util.*;
import java.util.concurrent.*;

public class BrowsePage implements Page {
    final private String[] genres = {"Crime", "Drama", "Biography", "History", "Sport", "Romance", "War", "Mystery", "Adventure", "Family", "Fantasy", "Thriller", "Horror", "Film-Noir", "Musical", "Sci-fi", "Comedy", "Action", "Western"};

    private Scene scene;
    private Boolean menuHover = false;
    private boolean movieSearch = true;
    private HashMap<String, String> searchFilters = new HashMap();
    private Timeline searchDelay = new Timeline(
            new KeyFrame(Duration.seconds(1),
                    event -> search()));
    private boolean imageLoadCooldown = false;
    private Timer timer = new Timer();

    final private VBox menuList = new VBox();
    final private FlowPane flowPane = new FlowPane();
    final private ScrollPane scrollPane = new ScrollPane();
    final private TextField searchField = new TextField();
    final private Label searchFilterLabel = new Label("");
    final private ImageView loadingGif = new ImageView("loading_medium.gif");
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
        scrollPane.vvalueProperty().addListener((obs) -> {
            if (!imageLoadCooldown) {
                imageLoadCooldown = true;
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {imageLoadCooldown=false;}
                };
                timer.schedule(task,50);
                fetchImages();}});
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
        searchField.setOnKeyTyped(keyEvent -> resetSearchDelay(searchField.getText()));
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
        final HBox menuBackground = new HBox();
        final ScrollPane menu = new ScrollPane();
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
        fadeInTransition.setNode(menuBackground);

        fadeOutTransition.setToValue(0);
        fadeOutTransition.setDuration(Duration.millis(250));
        fadeOutTransition.setDelay(Duration.millis(400));
        fadeOutTransition.setAutoReverse(false);
        fadeOutTransition.setOnFinished(actionEvent -> menuBackground.setVisible(false));
        fadeOutTransition.setNode(menuBackground);

        menuButtonPane.setOnMouseClicked(mouseEvent -> {
            if (menuBackground.isVisible()) {
                closeMenu();
            } else {
                mainMenuItems();
                menuBackground.setVisible(true);
                menu.setVisible(true);
                openMenuTransition.play();
                fadeInTransition.play();
            }

        });

        menu.setOnMouseEntered(mouseEvent -> menuHover = true);
        menu.setOnMouseExited(mouseEvent -> menuHover = false);
        menuBackground.setOnMouseClicked(mouseEvent -> {if (!menuHover) closeMenu();});

        mainMenuItems();
        menuAnchorPane.getChildren().add(menuList);
        menu.setContent(menuAnchorPane);
        menuBackground.getChildren().add(menu);
        stackPane.getChildren().add(menuBackground);
        menuBackground.setVisible(false);
        closeMenu();

        scene = new Scene(root);
    }

    private void updateFilterLabel() {
        if (searchFilters.size() > 0) {
            searchFilterLabel.setText(String.format("%s: %s", (movieSearch ? "Movies" : "TV Shows"), searchFilters.toString().replaceAll("(\\{)|(\\})", "").replaceAll("=",": ")));
        } else
            searchFilterLabel.setText((movieSearch ? "Movies" : "TV Shows"));
    }

    private void closeMenu() {
        closeMenuTransition.play();
        fadeOutTransition.play();
        updateFilterLabel();
        resetSearchDelay(searchField.getText());
    }

    private ObservableList<Node> getVisibleNodes(ScrollPane pane) {
        ObservableList<Node> visibleNodes = FXCollections.observableArrayList();
        Bounds paneBounds = pane.localToScene(pane.getBoundsInParent());
        if (pane.getContent() instanceof Parent) {
            for (Node n : ((Parent) pane.getContent()).getChildrenUnmodifiable()) {
                Bounds nodeBounds = n.localToScene(n.getBoundsInLocal());
                if (paneBounds.intersects(nodeBounds)) {
                    visibleNodes.add(n);
                }
            }
        }
        return visibleNodes;
    }

    private void mainMenuItems() {
        menuList.getChildren().clear();
        Label filterTitleLbl = new Label("Filters");
        filterTitleLbl.setTextFill(Color.GRAY);
        filterTitleLbl.setFont(new Font("Segoe UI Semibold Italic", 20));
        menuList.getChildren().add(filterTitleLbl);

        Label movieTVshowLabel = new Label((movieSearch ? "TV Shows" : "Movies"));
        movieTVshowLabel.setCursor(Cursor.HAND);
        movieTVshowLabel.setTextFill(Color.DARKGRAY);
        movieTVshowLabel.setFont(new Font("Segoe UI Thin", 24));
        movieTVshowLabel.setOnMouseClicked(mouseEvent -> {movieSearch=!movieSearch;closeMenu();});
        movieTVshowLabel.setOnMouseEntered(mouseEvent -> movieTVshowLabel.setTextFill(Color.WHITE));
        movieTVshowLabel.setOnMouseExited(mouseEvent -> movieTVshowLabel.setTextFill(Color.DARKGRAY));
        Label genreLabel = new Label("Genre");
        genreLabel.setCursor(Cursor.HAND);
        genreLabel.setTextFill(Color.DARKGRAY);
        genreLabel.setFont(new Font("Segoe UI Thin", 24));
        genreLabel.setOnMouseClicked(mouseEvent -> genreFilterUI());
        genreLabel.setOnMouseEntered(mouseEvent -> genreLabel.setTextFill(Color.WHITE));
        genreLabel.setOnMouseExited(mouseEvent -> genreLabel.setTextFill(Color.DARKGRAY));
        Label yearLabel = new Label("Year");
        yearLabel.setCursor(Cursor.HAND);
        yearLabel.setTextFill(Color.DARKGRAY);
        yearLabel.setFont(new Font("Segoe UI Thin", 24));
        yearLabel.setOnMouseClicked(mouseEvent -> yearFilterUI());
        yearLabel.setOnMouseEntered(mouseEvent -> yearLabel.setTextFill(Color.WHITE));
        yearLabel.setOnMouseExited(mouseEvent -> yearLabel.setTextFill(Color.DARKGRAY));
        Label scoreLabel = new Label("Score");
        scoreLabel.setCursor(Cursor.HAND);
        scoreLabel.setTextFill(Color.DARKGRAY);
        scoreLabel.setFont(new Font("Segoe UI Thin", 24));
        scoreLabel.setOnMouseClicked(mouseEvent -> scoreFilterUI());
        scoreLabel.setOnMouseEntered(mouseEvent -> scoreLabel.setTextFill(Color.WHITE));
        scoreLabel.setOnMouseExited(mouseEvent -> scoreLabel.setTextFill(Color.DARKGRAY));
        Label APIsearchLabel = new Label("API Search");
        APIsearchLabel.setCursor(Cursor.HAND);
        APIsearchLabel.setTextFill(Color.DARKGRAY);
        APIsearchLabel.setFont(new Font("Segoe UI Thin", 24));
        APIsearchLabel.setOnMouseClicked(mouseEvent -> {searchFilters.clear();searchFilters.put("API Search", searchField.getText());closeMenu();});
        APIsearchLabel.setOnMouseEntered(mouseEvent -> APIsearchLabel.setTextFill(Color.WHITE));
        APIsearchLabel.setOnMouseExited(mouseEvent -> APIsearchLabel.setTextFill(Color.DARKGRAY));
        Label clearLabel = new Label("Clear Filters");
        clearLabel.setCursor(Cursor.HAND);
        clearLabel.setTextFill(Color.web("#E08000"));
        clearLabel.setFont(new Font("Segoe UI Thin", 20));
        clearLabel.setOnMouseClicked(mouseEvent -> {searchFilters.clear();closeMenu();});
        clearLabel.setOnMouseEntered(mouseEvent -> clearLabel.setTextFill(Color.ORANGE));
        clearLabel.setOnMouseExited(mouseEvent -> clearLabel.setTextFill(Color.web("#E08000")));

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
            lbl.setTextFill(Color.DARKGRAY);
            lbl.setFont(new Font("Segoe UI Thin", 24));
            lbl.setOnMouseEntered(mouseEvent -> lbl.setTextFill(Color.WHITE));
            lbl.setOnMouseExited(mouseEvent -> lbl.setTextFill(Color.DARKGRAY));
            int finalI = i;
            lbl.setOnMouseClicked(mouseEvent -> {searchFilters.put("Year",String.valueOf(1900+finalI*10));closeMenu();});
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
            lbl.setTextFill(Color.DARKGRAY);
            lbl.setFont(new Font("Segoe UI Thin", 24));
            lbl.setOnMouseEntered(mouseEvent -> lbl.setTextFill(Color.WHITE));
            lbl.setOnMouseExited(mouseEvent -> lbl.setTextFill(Color.DARKGRAY));
            lbl.setOnMouseClicked(mouseEvent -> {searchFilters.put("Genre",g);closeMenu();});
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
            Label lbl = new Label(String.format("%s / 10", i));
            lbl.setCursor(Cursor.HAND);
            lbl.setTextFill(Color.DARKGRAY);
            lbl.setFont(new Font("Segoe UI Thin", 24));
            lbl.setOnMouseEntered(mouseEvent -> lbl.setTextFill(Color.WHITE));
            lbl.setOnMouseExited(mouseEvent -> lbl.setTextFill(Color.DARKGRAY));
            int finalI = i;
            lbl.setOnMouseClicked(mouseEvent -> {searchFilters.put("Score",String.valueOf(finalI));closeMenu();});
            menuList.getChildren().add(lbl);
        }
    }

    public void resetSearchDelay(String searchQuery) {
        if (searchFilters.containsKey("API Search")) {
            searchFilters.clear();
            searchFilters.put("API Search", searchQuery);
        } else
            if (searchQuery.length() < 1)
                searchFilters.remove("Title");
            else
                searchFilters.put("Title",searchQuery);
        loadingGif.setVisible(true);
        updateFilterLabel();
        searchDelay.stop();
        searchDelay.play();
    }

    private void search() {
        String searchType = (movieSearch ? "Movies" : "Shows");
        flowPane.getChildren().clear();
        ExecutorService searchThread = Executors.newSingleThreadExecutor();
        Future<ArrayList<Content>> searchResult = searchThread.submit(StreamingService.getInstance().search(searchType, searchFilters));

        Thread t = new Thread(() -> {
            try {
                ArrayList<Content> result = searchResult.get(1,TimeUnit.SECONDS);
                if (result == null || result.size() < 1)
                    Platform.runLater(() -> noResultsElement((searchFilters.get("Title") == null ? searchFilters.get("API Search") : searchFilters.get("Title"))));
                else {
                    for (Content cover : result) {
                        TimeUnit.MILLISECONDS.sleep(10);
                        Platform.runLater(() -> addCover(cover));
                    }
                    Platform.runLater(() -> {fetchImages();loadingGif.setVisible(false);});
                }
            } catch (Exception e) {}
        });
        t.setDaemon(true);
        t.start();
    }

    private void fetchImages() {
        ObservableList<Node> coverList = getVisibleNodes(scrollPane);
        if (coverList != null && coverList.size() > 0) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(coverList.size());
            for (Node covImg : coverList)
                if (covImg instanceof CoverImage && !((CoverImage) covImg).isImageLoaded())
                    executor.execute(((CoverImage) covImg).fetchImage);
            executor.shutdown();
        }
    }

    private void addCover(Content result) {
        CoverImage imgView = new CoverImage(new Image("placeholder.jpg"), result, 0.65f);
        imgView.setPreserveRatio(true);
        imgView.setFitWidth(250);
        imgView.setEffect(new DropShadow());
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
        imgView.setOnMouseClicked(MouseEvent -> {
            if (imgView.getInfo() instanceof MovieContent)
                StreamingService.getInstance().addPage(new MoviePage((MovieContent)imgView.getInfo()));
            else if (imgView.getInfo() instanceof SeriesContent)
                StreamingService.getInstance().addPage(new SeriesPage((SeriesContent)imgView.getInfo()));
            else if (imgView.getInfo() instanceof ExternalContent)
                StreamingService.getInstance().addPage(new ExternalPage((ExternalContent)imgView.getInfo()));
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Cannot find data for content!");
                alert.showAndWait()
                        .filter(response -> response == ButtonType.OK)
                        .ifPresent(response -> alert.close());
            }
        });
        flowPane.getChildren().add(imgView);
    }

    private void noResultsElement(String query) {
        Label lbl = new Label(String.format("No results for \"%s\" in %s", query, (!movieSearch ? "TV Shows" : "Movies")));
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(new Font("Segoe UI Semibold", 24));
        FlowPane.setMargin(lbl, new Insets(50, 0, 0, 0));
        flowPane.getChildren().add(lbl);
    }

    @Override
    public Scene getScene() {
        fetchImages();
        return scene;
    }
}
