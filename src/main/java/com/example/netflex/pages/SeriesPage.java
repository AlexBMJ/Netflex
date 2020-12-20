package com.example.netflex.pages;

import com.example.netflex.content.Content;
import com.example.netflex.content.EpisodeContent;
import com.example.netflex.content.SeriesContent;
import com.example.netflex.database.Search;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class SeriesPage implements Page {
    private Scene scene;
    private SeriesContent series;

    final private Label searchFilterLabel = new Label("");
    final private ImageView loadingGif = new ImageView("loading_medium.gif");

    private ChoiceBox seasonSelector = new ChoiceBox();
    private VBox episodeContainer = new VBox();

    public SeriesPage(SeriesContent series) {
        this.series = series;
        // Root
        final VBox root = new VBox();
        root.setFillWidth(true);
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: rgb(30,30,30);");

        // Header
        final GridPane gp1 = new GridPane();
        final ImageView backButton = new ImageView("back.png");
        final Pane backButtonPane = new Pane(backButton);
        final ImageView logo = new ImageView("netflex_logo.png");
        backButton.setPreserveRatio(true);
        backButton.setFitHeight(40);
        backButtonPane.setCursor(Cursor.HAND);
        logo.setPreserveRatio(true);
        logo.setFitHeight(40);
        GridPane.setMargin(backButtonPane, new Insets(5,15,5,15));
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
        gp1.add(backButtonPane,0,0);
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
        VBox mainBox = new VBox();
        mainBox.setStyle("-fx-background-color: rgb(30,30,30);");
        mainBox.setFillWidth(true);
        mainBox.setAlignment(Pos.TOP_CENTER);
        scrollPane.setContent(mainBox);
        scrollPane.setStyle("-fx-background-color: rgb(30,30,30);");
        scrollPane.getStyleClass().add("scrollPane");
        scrollPane.getStylesheets().add("scrollbar.css");
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        stackPane.getChildren().add(scrollPane);

        // Title, Score, Length Container
        HBox hbox1 = new HBox();
        hbox1.setSpacing(10);
        VBox.setMargin(hbox1, new Insets(20,20,0,20));
        hbox1.setAlignment(Pos.TOP_CENTER);
        mainBox.getChildren().add(hbox1);

        // Title
        Text titleText = new Text(series.getTitle());
        titleText.setFill(Color.WHITE);
        titleText.setFont(new Font("Segoe UI Bold", 40));
        hbox1.getChildren().add(titleText);

        // Year
        Text yearText = new Text(String.valueOf(series.getYear()));
        yearText.setFill(Color.DARKGRAY);
        yearText.setFont(new Font("Segoe UI", 40));
        hbox1.getChildren().add(yearText);

        // Score
        Text scoreText = new Text(String.valueOf(series.getScore()));
        scoreText.setFill(Color.DARKGRAY);
        scoreText.setFont(new Font("Segoe UI Bold", 20));
        HBox.setMargin(scoreText, new Insets(20,0,0,20));
        hbox1.getChildren().add(scoreText);

        // Length
        Text lengthText = new Text(series.getLength());
        lengthText.setFill(Color.DARKGRAY);
        lengthText.setFont(new Font("Segoe UI Italic", 20));
        HBox.setMargin(lengthText, new Insets(20,0,0,5));
        hbox1.getChildren().add(lengthText);



        // Cover, Summary Container
        HBox hbox2 = new HBox();
        VBox.setMargin(hbox2, new Insets(20));
        hbox2.setAlignment(Pos.TOP_CENTER);
        VBox vbox1 = new VBox();
        mainBox.getChildren().add(hbox2);

        // Cover
        ImageView cover = new ImageView(series.getImage());
        DropShadow ds = new DropShadow();
        ds.setRadius(20);
        cover.setEffect(ds);
        cover.setPreserveRatio(true);
        cover.setFitWidth(450);
        cover.setSmooth(true);
        HBox.setMargin(cover, new Insets(0,25,0,0));
        hbox2.getChildren().add(cover);

        // Summary
        Text summaryText = new Text(series.getSummary());
        VBox.setMargin(vbox1, new Insets(20));
        summaryText.setWrappingWidth(450);
        summaryText.setFill(Color.DARKGRAY);
        summaryText.setFont(new Font("Segoe UI", 20));
        vbox1.getChildren().add(summaryText);

        // People
        Text stars = new Text("Starring: " + Arrays.asList(series.getStars()).stream().collect(Collectors.joining(",\r               ")));
        VBox.setMargin(stars, new Insets(20));
        stars.setFill(Color.DARKGRAY);
        stars.setFont(new Font("Segoe UI", 20));
        vbox1.getChildren().add(stars);

        if (series.getWriters().length > 0) {
            Text writers = new Text("Writers: " + Arrays.asList(series.getWriters()).stream().collect(Collectors.joining(",\r             ")));
            VBox.setMargin(writers, new Insets(20));
            writers.setFill(Color.DARKGRAY);
            writers.setFont(new Font("Segoe UI", 20));
            vbox1.getChildren().add(writers);
        }

        // Genres
        Text genreText = new Text("Genre: " + Arrays.asList(series.getGenres()).stream().collect(Collectors.joining("\r         ")));
        VBox.setMargin(genreText, new Insets(20));
        genreText.setFill(Color.DARKGRAY);
        genreText.setFont(new Font("Segoe UI", 20));
        vbox1.getChildren().add(genreText);

        hbox2.getChildren().add(vbox1);

        // Dropdown
        Text episodesTitleText = new Text("Episodes");
        episodesTitleText.setFill(Color.WHITE);
        episodesTitleText.setFont(new Font("Segoe UI Bold", 30));
        VBox.setMargin(episodesTitleText, new Insets(20));
        mainBox.getChildren().add(episodesTitleText);
        String[] seasonDropdownText = new String[series.getSeasons().size()];
        for (int s = 0; s < series.getSeasons().size(); s++) {
            seasonDropdownText[s] = String.format("Season %s", s+1);
        }
        seasonSelector.setItems(FXCollections.observableArrayList(seasonDropdownText));
        seasonSelector.getSelectionModel().select(0);
        seasonSelector.getStyleClass().add("choice-box");
        seasonSelector.getStylesheets().add("dropdown.css");
        getEpisodes(1);
        // On Item Change
        seasonSelector.getSelectionModel().selectedIndexProperty().addListener((ov, value, new_value) -> getEpisodes(new_value.intValue()+1));
        VBox.setMargin(seasonSelector, new Insets(20));
        mainBox.getChildren().add(seasonSelector);

        // Episodes
        episodeContainer.setAlignment(Pos.TOP_CENTER);
        episodeContainer.setSpacing(5);
        episodeContainer.setFillWidth(false);
        VBox.setMargin(episodeContainer, new Insets(20));
        mainBox.getChildren().add(episodeContainer);

        // Loading Image
        loadingGif.setBlendMode(BlendMode.SCREEN);
        loadingGif.setPreserveRatio(true);
        loadingGif.setFitWidth(400);
        loadingGif.setVisible(false);
        StackPane.setMargin(loadingGif, new Insets(0, 0, 20,0));
        StackPane.setAlignment(loadingGif, Pos.BOTTOM_CENTER);
        stackPane.getChildren().add(loadingGif);

        backButtonPane.setOnMouseClicked(mouseEvent -> {
            try {
                StreamingService.getInstance().prevPage();
            } catch (PageCacheException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "No more previous pages!");
                alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> alert.close());
            }
        });

        scene = new Scene(root);
    }

    private void getEpisodes(int seasonNumber) {
        loadingGif.setVisible(true);
        HashMap<String, String> filter = new HashMap();
        filter.put("ShowID",series.getId());
        filter.put("Season",String.valueOf(seasonNumber));
        Search search = new Search("Episodes", filter);
        search.setOnCompleted(() -> {ArrayList<Content> r = search.getResult();Platform.runLater(() -> updateEpisodes(r));});
        search.setOnFailed(() -> StreamingService.getInstance().showErrorMessage(search.getError()));
        search.run();
    }

    private void updateEpisodes(ArrayList<Content> result) {
        episodeContainer.getChildren().clear();
        for (Content content : result) {
            EpisodeContent ep = (EpisodeContent)content;
            HBox epBox = new HBox();
            epBox.setAlignment(Pos.TOP_LEFT);
            VBox.setMargin(epBox, new Insets(0,5,25,20));
            epBox.setSpacing(25);

            // Title
            VBox vb = new VBox();
            Text epName = new Text("Episode " + ep.getEpisodeNumber() + ": " + ep.getTitle());
            epName.setFill(Color.WHITE);
            epName.setFont(new Font("Segoe UI Bold", 24));
            VBox.setMargin(epName, new Insets(5,0,5,0));
            vb.getChildren().add(epName);

            // Thumbnail
            ImageView thumbnail = new ImageView(ep.getImage());
            DropShadow ds = new DropShadow();
            ds.setRadius(5);
            thumbnail.setEffect(ds);
            thumbnail.setPreserveRatio(true);
            thumbnail.setFitWidth(400);
            thumbnail.setSmooth(true);
            HBox.setMargin(thumbnail, new Insets(5));

            // Summary
            Text summaryText = new Text(ep.getSummary() + "\r\rLength: " + ep.getLength());
            summaryText.setWrappingWidth(600);
            summaryText.setFill(Color.DARKGRAY);
            summaryText.setFont(new Font("Segoe UI", 20));
            vb.getChildren().add(summaryText);


            epBox.setAlignment(Pos.CENTER_LEFT);
            epBox.getChildren().add(thumbnail);
            epBox.getChildren().add(vb);
            episodeContainer.getChildren().add(epBox);
        }
        loadingGif.setVisible(false);
    }

    @Override
    public Scene getScene() {
        return scene;
    }
}
