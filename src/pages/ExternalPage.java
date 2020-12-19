package pages;

import content.ExternalContent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ExternalPage implements Page {
    private Scene scene;
    private ExternalContent series;

    final Label searchFilterLabel = new Label("");
    final ImageView loadingGif = new ImageView("loading_medium.gif");

    public ExternalPage(ExternalContent series) {
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
        mainBox.setAlignment(Pos.TOP_LEFT);
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

        // Cover, Summary Container
        HBox hbox2 = new HBox();
        hbox2.setAlignment(Pos.CENTER);
        VBox vbox1 = new VBox();
        hbox2.getChildren().add(vbox1);
        mainBox.getChildren().add(hbox2);

        // Cover
        ImageView cover = new ImageView(series.getImage());
        cover.setPreserveRatio(true);
        cover.setFitWidth(450);
        cover.setSmooth(true);
        VBox.setMargin(cover, new Insets(20,0,20,0));
        vbox1.getChildren().add(cover);

        // Summary
        Text summaryText = new Text(series.getSummary());
        VBox.setMargin(summaryText, new Insets(0,0,20,0));
        summaryText.setWrappingWidth(450);
        summaryText.setFill(Color.DARKGRAY);
        summaryText.setFont(new Font("Segoe UI", 20));
        vbox1.getChildren().add(summaryText);

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

    @Override
    public Scene getScene() {
        return scene;
    }
}
