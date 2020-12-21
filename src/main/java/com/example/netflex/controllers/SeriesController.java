package com.example.netflex.controllers;

import com.example.netflex.StreamingService;
import com.example.netflex.content.Content;
import com.example.netflex.content.SeriesContent;
import com.example.netflex.database.Search;
import com.example.netflex.pages.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.HashMap;

public class SeriesController implements Controller {
    private SeriesPage page;
    private SeriesContent series;

    public SeriesController(SeriesContent content) {
        page = new SeriesPage(content);
        this.series = content;
        page.seasonSelector.getSelectionModel().selectedIndexProperty().addListener((ov, value, new_value) -> getEpisodes(new_value.intValue()+1));

        page.backButtonPane.setOnMouseClicked(mouseEvent -> {
            try {
                StreamingService.getInstance().prevPage();
            } catch (PageCacheException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "No more previous pages!");
                alert.showAndWait()
                        .filter(response -> response == ButtonType.OK)
                        .ifPresent(response -> alert.close());
            }
        });
        getEpisodes(1);
    }

    private void getEpisodes(int seasonNumber) {
        page.setLoading(true);
        HashMap<String, String> filter = new HashMap();
        filter.put("ShowID",series.getId());
        filter.put("Season",String.valueOf(seasonNumber));
        Search search = new Search("Episodes", filter);
        search.setOnCompleted(() -> {
            ArrayList<Content> r = search.getResult();
            Platform.runLater(() -> page.updateEpisodes(r));});
        search.setOnFailed(() -> showErrorMessage(search.getError()));
        search.run();
    }

    private void showErrorMessage(String errorMsg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Search Failed! " + errorMsg);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> alert.close());});
    }

    @Override
    public Page getPage() {
        return page;
    }
}
