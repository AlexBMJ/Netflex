package com.example.netflex.controllers;

import com.example.netflex.StreamingService;
import com.example.netflex.content.MovieContent;
import com.example.netflex.pages.*;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class MovieController implements Controller {
    private MoviePage page;
    private MovieContent movie;

    public MovieController(MovieContent content) {
        this.page = new MoviePage(content);
        this.movie = content;
        page.backButtonPane.setOnMouseClicked(mouseEvent -> {
            try {
                page.trailer.getEngine().reload();
                StreamingService.getInstance().prevPage();
            } catch (PageCacheException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "No more previous pages!");
                alert.showAndWait()
                        .filter(response -> response == ButtonType.OK)
                        .ifPresent(response -> alert.close());
            }
        });
    }


    @Override
    public Page getPage() {
        return page;
    }
}
