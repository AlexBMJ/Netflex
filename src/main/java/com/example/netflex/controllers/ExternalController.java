package com.example.netflex.controllers;

import com.example.netflex.content.ExternalContent;
import com.example.netflex.pages.ExternalPage;
import com.example.netflex.pages.Page;
import com.example.netflex.pages.PageCacheException;
import com.example.netflex.StreamingService;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ExternalController implements Controller {
    private ExternalPage page;
    private ExternalContent content;

    public ExternalController(ExternalContent content) {
        this.page = new ExternalPage(content);
        this.content = content;

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

    }

    @Override
    public Page getPage() {
        return page;
    }
}
