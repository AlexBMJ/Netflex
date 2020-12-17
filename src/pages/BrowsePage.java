package pages;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import content.*;
import database.AlgoliaAPI;
import database.SearchDatabase;
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
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pages.components.CoverImage;

import java.lang.ref.Cleaner;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        int actualHits = 0;
        if (searchSource == "local") {
            String type = (prevSearchTerm[1] == "Movie" ? "Movies" : "Shows");
            ResultSet results = SearchDatabase.search(prevSearchTerm[0], type, searchFilters);
            if (type == "Movies") {
                for (MovieContent movie : parseMovieResult(results)) {
                    actualHits++;
                    Platform.runLater(() -> coverList.add(addCoverElement(movie)));
                }
            } else if (type == "Shows") {
                for (SeriesContent show : parseSeriesResult(results)) {
                    actualHits++;
                    Platform.runLater(() -> coverList.add(addCoverElement(show)));
                }
            }
        } else if (searchSource == "external") {
            List<TVDBResult> hits = AlgoliaAPI.getSeriesHits(prevSearchTerm[0], prevSearchTerm[1], 30);
            for (TVDBResult result : hits)
                if (isValidAPIData(result)) {
                    actualHits++;
                    Platform.runLater(() -> coverList.add(addCoverElement(new ExternalContent(result))));
                }
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
        //Initilaize DB
        SearchDatabase.connect();

        // Root
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

        // Search Field
        searchField.setMaxWidth(300);
        searchField.setMaxHeight(25);
        searchField.setPromptText("Search");
        searchField.setStyle("-fx-background-color: rgb(20,20,20); -fx-text-fill: white");
        searchField.setOpacity(0.8f);
        searchField.setAlignment(Pos.TOP_CENTER);
        StackPane.setMargin(searchField, new Insets(20,0,0,0));
        searchField.setOnKeyTyped(keyEvent -> checkSearchTimer());
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

        scene = new Scene(root);
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
        checkSearchTimer();
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
        APIsearchLabel.setOnMouseClicked(mouseEvent -> {searchFilters.clear();searchSource="external";closeMenu();});
        APIsearchLabel.setOnMouseEntered(mouseEvent -> APIsearchLabel.setTextFill(Color.WHITE));
        APIsearchLabel.setOnMouseExited(mouseEvent -> APIsearchLabel.setTextFill(Color.DARKGRAY));
        Label clearLabel = new Label("Clear Filters");
        clearLabel.setCursor(Cursor.HAND);
        clearLabel.setTextFill(Color.web("#E08000"));
        clearLabel.setFont(new Font("Segoe UI Thin", 20));
        clearLabel.setOnMouseClicked(mouseEvent -> {searchFilters.clear();searchSource="local";closeMenu();});
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
            lbl.setOnMouseClicked(mouseEvent -> {searchFilters.put("Year",String.valueOf(190+finalI));searchSource="local";closeMenu();});
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
            lbl.setOnMouseClicked(mouseEvent -> {searchFilters.put("Genre",g);searchSource="local";closeMenu();});
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
            lbl.setOnMouseClicked(mouseEvent -> {searchFilters.put("Score",String.valueOf(finalI)+",");searchSource="local";closeMenu();});
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

    private ArrayList<MovieContent> parseMovieResult(ResultSet results) {
        try {
            System.out.println(results.next());
            System.out.println(results.getString("Title"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        ArrayList movies = new ArrayList();
        try {
            while (results.next()) {
                System.out.println("MOVIE2");
                ObjectMapper mapper = new ObjectMapper();
                MovieContent movie = new MovieContent(
                        results.getString("MovieID"),
                        results.getString("Title"),
                        results.getString("Summary"),
                        results.getString("Length"),
                        Float.parseFloat(results.getString("Score").replace(',', '.')),
                        Integer.parseInt(results.getString("Year")),
                        results.getString("Genres").split(", "),
                        mapper.readValue(results.getString("Writers"), String[].class),
                        mapper.readValue(results.getString("Stars"), String[].class),
                        mapper.readValue(results.getString("IMAGE"), byte[].class),
                        results.getString("Trailer")
                );
                movies.add(movie);
                System.out.println(movie.getTitle());
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return movies;
    }

    private ArrayList<SeriesContent> parseSeriesResult(ResultSet results) {
        ArrayList movies = new ArrayList();
        Pattern regx = Pattern.compile("(\\d\\d\\d\\d)");

            try {
            while (results.next()) {
                ObjectMapper mapper = new ObjectMapper();
                Matcher m = regx.matcher(results.getString("Year"));
                SeriesContent movie = new SeriesContent(
                        results.getString("ShowID"),
                        results.getString("Title"),
                        results.getString("Summary"),
                        results.getString("Length"),
                        Float.parseFloat(results.getString("Score").replace(',', '.')),
                        (m.find() ? Integer.parseInt(m.group(0)) : 0),
                        (m.find() ? Integer.parseInt(m.group(0)) : 0),
                        results.getString("Genres").split(", "),
                        mapper.readValue(results.getString("Writers"), String[].class),
                        mapper.readValue(results.getString("Stars"), String[].class),
                        mapper.readValue(results.getString("IMAGE"), byte[].class),
                        mapper.readValue(results.getString("Seasons"), String[][].class)
                );
                movies.add(movie);

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return movies;
    }

    private CoverImage addCoverElement(Content result) {
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
