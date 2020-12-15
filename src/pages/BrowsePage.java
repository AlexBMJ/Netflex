package pages;


import content.TVDBResult;
import database.AlgoliaAPI;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import pages.components.CoverImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BrowsePage implements Page {
    final private String[] genres = {"Crime", "Drama", "Biography", "History", "Sport", "Romance", "War", "Mystery", "Adventure", "Family", "Fantasy", "Thriller", "Horror", "Film-Noir", "Musical", "Sci-fi", "Comedy", "Action", "Western"};

    private Scene scene;
    private Boolean menuHover = false;
    private String searchSource = "local";
    private boolean movieSearch = true;
    private HashMap<String, String> searchFilters = new HashMap();
    private String[] prevSearchTerm = {"", "Movie"};
    private Timeline searchDelay = new Timeline(
            new KeyFrame(Duration.seconds(1),
                    event -> searchAPI()));

    FadeTransition fadeOutTransition = new FadeTransition();
    TranslateTransition closeMenuTransition = new TranslateTransition();

    final VBox menuList = new VBox();
    final FlowPane flowPane = new FlowPane();
    final TextField searchField = new TextField();
    final Label searchFilterLabel = new Label("");
    final ImageView loadingGif = new ImageView("loading_medium.gif");


    private Runnable searchRun = () -> {
        loadingGif.setVisible(true);
        List<CoverImage> coverList = new ArrayList();
        List<TVDBResult> hits = AlgoliaAPI.getSeriesHits(prevSearchTerm[0], prevSearchTerm[1], 30);
        int actualHits = 0;
        for (TVDBResult result : hits)
            if (isValidAPIData(result)) {
                actualHits++;
                Platform.runLater(() -> coverList.add(addCoverElement(result)));
            }
        if (actualHits > 0) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(actualHits);
            while (coverList.size() < actualHits) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (CoverImage covimg : coverList)
                executor.execute(covimg.fetchImage);
            executor.shutdown();
        } else {
            Platform.runLater(() -> noResultsElement());
        }
        Platform.runLater(() -> loadingGif.setVisible(false));
        return;
    };

    public BrowsePage() {
        final VBox root = new VBox();
        root.setFillWidth(true);
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: rgb(90,30,30);");

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
        final ScrollPane scrollPane = new ScrollPane();
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
        searchField.setMaxWidth(300);
        searchField.setMaxHeight(25);
        searchField.setPromptText("Search");
        searchField.setStyle("-fx-background-color: rgb(20,20,20); -fx-text-fill: white");
        searchField.setOpacity(0.8f);
        searchField.setAlignment(Pos.TOP_CENTER);
        StackPane.setMargin(searchField, new Insets(15,0,0,0));
        searchField.setOnKeyTyped(keyEvent -> search());
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
        loadingGif.setPreserveRatio(true);
        loadingGif.setFitWidth(100);
        loadingGif.setY(100);
        stackPane.getChildren().add(loadingGif);

        scene = new Scene(root);
    }

    private void search() {
        if (searchSource == "external")
            checkSearchTimer();
        else
            System.out.println(searchField.getText() + " | WIP"); // TODO: IMPLEMENT LOCAL SEARCH
    }

    private void initSearchFilters() {
        if (searchSource == "external")
            searchFilterLabel.setText((movieSearch ? "Movies" : "TV Shows") + ": API Search");
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
        search();
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
            lbl.setOnMouseClicked(mouseEvent -> {searchFilters.put("year",String.valueOf(1900+finalI*10));searchSource="local";closeMenu();});
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
            lbl.setOnMouseClicked(mouseEvent -> {searchFilters.put("genre",g);searchSource="local";closeMenu();});
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
            lbl.setOnMouseClicked(mouseEvent -> {searchFilters.put("score",String.valueOf(finalI));searchSource="local";closeMenu();});
            menuList.getChildren().add(lbl);
        }
    }

    public void checkSearchTimer() {
        loadingGif.setVisible(true);
        searchDelay.stop();
        searchDelay.play();
    }

    private void searchAPI() {
        String searchTerm = searchField.getText();
        String searchType = (movieSearch ? "Movie" : "TV");
        if (searchTerm.length() > 1)
            if (!prevSearchTerm[0].equals(searchTerm) || !prevSearchTerm[1].equals(searchType)) {
                prevSearchTerm[0] = searchTerm;
                prevSearchTerm[1] = searchType;
                flowPane.getChildren().clear();
                Thread t = new Thread(searchRun);
                t.setDaemon(true);
                t.start();
            } else
                loadingGif.setVisible(false);
    }

    private boolean isValidAPIData(TVDBResult result) {
        return (result.getImage() != null &&
                result.getImage().length() >= 1 &&
                !result.getImage().equals("https://artworks.thetvdb.com/banners/images/missing/movie.jpg") &&
                !result.getImage().equals("https://artworks.thetvdb.com/banners/images/missing/series.jpg") &&
                result.getOverviews() != null &&
                result.getOverviews().containsKey("eng"));
    }

    private CoverImage addCoverElement(TVDBResult result) {
        CoverImage imgView = new CoverImage(new Image("placeholder.png"), result);
        imgView.setPreserveRatio(true);
        imgView.setFitWidth(250);
        imgView.setEffect(new DropShadow());
        imgView.setCursor(Cursor.HAND);

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
        flowPane.getChildren().add(imgView);
        return imgView;
    }

    private void noResultsElement() {
        Label lbl = new Label(String.format("No results for \"%s\" in %s", prevSearchTerm[0], movieSearch ? "Movies" : "TV Shows"));
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(new Font("Segoe UI Semibold", 24));
        FlowPane.setMargin(lbl, new Insets(50, 0, 0, 0));
        flowPane.getChildren().add(lbl);
    }

    public Scene getScene() {
        return scene;
    }
}
