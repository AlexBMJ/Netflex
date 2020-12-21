package com.example.netflex.controllers;

import com.example.netflex.content.Content;
import com.example.netflex.content.ExternalContent;
import com.example.netflex.content.MovieContent;
import com.example.netflex.content.SeriesContent;
import com.example.netflex.database.Search;
import com.example.netflex.pages.BrowsePage;
import com.example.netflex.StreamingService;
import com.example.netflex.pages.Page;
import com.example.netflex.pages.components.CoverImage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BrowseController implements Controller {
    private BrowsePage page;

    final private String[] genres = {"Crime", "Drama", "Biography", "History", "Sport", "Romance", "War", "Mystery", "Adventure", "Family", "Fantasy", "Thriller", "Horror", "Film-Noir", "Musical", "Sci-fi", "Comedy", "Action", "Western"};
    public HashMap<String, String> searchFilters = new HashMap();
    public boolean movieSearch = true;
    private boolean menuHover = false;
    private Timeline searchDelay = new Timeline(
            new KeyFrame(Duration.seconds(1),
                    event -> getCovers()));
    private boolean imageLoadCooldown = false;
    private Timer timer = new Timer();

    public BrowseController() {
        page = new BrowsePage();
        page.searchField.setOnKeyTyped(keyEvent -> resetSearchDelay(page.searchField.getText()));

        page.scrollPane.vvalueProperty().addListener((obs) -> {
            if (!imageLoadCooldown) {
                imageLoadCooldown = true;
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {imageLoadCooldown=false;}
                };
                timer.schedule(task,50);
                fetchImages();}
        });

        page.menu.setOnMouseEntered(mouseEvent -> menuHover = true);
        page.menu.setOnMouseExited(mouseEvent -> menuHover = false);
        page.menuBackground.setOnMouseClicked(mouseEvent -> {if (!menuHover) closeMenu();});

        page.menuButtonPane.setOnMouseClicked(mouseEvent -> {
            if (page.menuBackground.isVisible()) {
                closeMenu();
            } else {
                filterMainMenu();
                page.openMenu();
            }

        });
        resetSearchDelay("");
    }

    private void getCovers() {
        String contentType = (movieSearch ? "Movies" : "Shows");
        Search search = new Search(contentType, searchFilters);
        search.addOnCompleted(() -> {
            try {
                updateCovers(search.getResult());
            } catch (InterruptedException e) {
                showErrorMessage(e.getMessage());
            }
        });
        search.addOnFailed(() -> showErrorMessage(search.getError()));
        search.run();
    }

    private void updateCovers(ArrayList<Content> result) throws InterruptedException {
        Platform.runLater(() -> page.clearFlowPane());
        if (result == null || result.size() < 1)
            Platform.runLater(() -> page.setNoResultsElement((searchFilters.get("Title") == null ? searchFilters.get("API Search") : searchFilters.get("Title")), movieSearch));
        else {
            for (Content content : result) {
                TimeUnit.MILLISECONDS.sleep(10);
                EventHandler<MouseEvent> clickAction;
                if (content instanceof MovieContent)
                    clickAction = mouseEvent -> StreamingService.getInstance().addPage(new MovieController((MovieContent)content));
                else if (content instanceof SeriesContent)
                    clickAction = mouseEvent -> StreamingService.getInstance().addPage(new SeriesController((SeriesContent)content));
                else if (content instanceof ExternalContent)
                    clickAction = mouseEvent -> StreamingService.getInstance().addPage(new ExternalController((ExternalContent)content));
                else
                    clickAction = mouseEvent -> showErrorMessage("Cannot find data for content!");

                Platform.runLater(() -> page.addCover(content, clickAction));
            }
            Platform.runLater(() -> {
                fetchImages();
                page.setLoading(false);
            });
        }
    }

    private ObservableList<Node> getVisibleCovers() {
        ObservableList<Node> visibleNodes = FXCollections.observableArrayList();
        Bounds paneBounds = page.scrollPane.localToScene(page.scrollPane.getBoundsInParent());
        if (page.scrollPane.getContent() instanceof Parent) {
            for (Node n : ((Parent) page.scrollPane.getContent()).getChildrenUnmodifiable()) {
                Bounds nodeBounds = n.localToScene(n.getBoundsInLocal());
                if (paneBounds.intersects(nodeBounds)) {
                    visibleNodes.add(n);
                }
            }
        }
        return visibleNodes;
    }

    private void fetchImages() {
        ObservableList<Node> coverList = getVisibleCovers();
        if (coverList != null && coverList.size() > 0) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(coverList.size());
            for (Node covImg : coverList)
                if (covImg instanceof CoverImage && !((CoverImage) covImg).isImageLoaded())
                    executor.execute(((CoverImage) covImg).fetchImage);
            executor.shutdown();
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
        page.setLoading(true);
        closeMenu();
        searchDelay.stop();
        searchDelay.play();
    }

    private void updateFilterLabel() {
        String labelText = movieSearch ? "Movies" : "TV Shows";;
        if (searchFilters.size() > 0)
            labelText = String.format("%s: %s", labelText, searchFilters.toString().replaceAll("(\\{)|(\\})", "").replaceAll("=",": "));
        page.updateFilterLabel(labelText);
    }

    private void filterMainMenu() {
        LinkedHashMap<String, EventHandler<MouseEvent>> subMenus = new LinkedHashMap();
        subMenus.put(movieSearch ? "TV Shows" : "Movies", mouseEvent -> {movieSearch=!movieSearch;resetSearchDelay(page.searchField.getText());});
        subMenus.put("Genre", mouseEvent -> {filterByGenre();});
        subMenus.put("Year", mouseEvent -> {filterByYear();});
        subMenus.put("Score", mouseEvent -> {filterByScore();});
        subMenus.put("API Search", mouseEvent -> {searchFilters.clear();searchFilters.put("API Search", page.searchField.getText());resetSearchDelay(page.searchField.getText());});
        page.setMainMenuItems(mouseEvent -> {searchFilters.clear();resetSearchDelay(page.searchField.getText());}, subMenus);
    }

    private void filterByYear() {
        LinkedHashMap<String, EventHandler<MouseEvent>> items = new LinkedHashMap();
        for (int i=2; i<=12; i++) {
            int finalI = i;
            items.put(String.format("%s", 1900 + i * 10), mouseEvent -> {
                searchFilters.put("Year", String.format("%s", 1900 + finalI * 10));
                resetSearchDelay(page.searchField.getText());
        });
        }
        page.setMenuItems("Year", mouseEvent -> filterMainMenu(), items);
    }

    private void filterByGenre() {
        LinkedHashMap<String, EventHandler<MouseEvent>> items = new LinkedHashMap();
        for (String g : genres) {
            items.put(g, mouseEvent -> {
                searchFilters.put("Genre", g);
                resetSearchDelay(page.searchField.getText());
            });
        }
        page.setMenuItems("Genre", mouseEvent -> filterMainMenu(),items);
    }

    private void filterByScore() {
        LinkedHashMap<String, EventHandler<MouseEvent>> items = new LinkedHashMap();
        for (int i=1; i<=10; i++) {
            int finalI = i;
            items.put(String.format("%s / 10", i), mouseEvent -> {
                searchFilters.put("Score", String.format("%s / 10", finalI));
                resetSearchDelay(page.searchField.getText());
            });
        }
        page.setMenuItems("Score", mouseEvent -> filterMainMenu(), items);
    }

    private void closeMenu() {
        updateFilterLabel();
        page.closeMenu();
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
        fetchImages();
        return page;
    }
}

