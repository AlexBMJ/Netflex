package com.example.netflex.pages;

import javafx.stage.Stage;

import java.util.LinkedList;

public class StreamingService {
    private static StreamingService service;
    private LinkedList<Page> pageCache;
    private Stage stage;
    private int cacheSize;
    private Page currentPage;

    private StreamingService(Stage stage, int cacheSize) {
        this.cacheSize = cacheSize;
        this.stage = stage;
        this.pageCache = new LinkedList();
    }

    public void addPage(Page page) {
        if (pageCache.size() >= cacheSize) {
            pageCache.remove(pageCache.size()-1);
        }
        if (this.currentPage != null)
            pageCache.add(0, this.currentPage);
        this.currentPage = page;
        stage.setScene(this.currentPage.getScene());
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            stage.setMaximized(true);
        } else
            stage.setHeight(stage.getHeight()-1);
        stage.show();
    }

    public void prevPage() throws PageCacheException {
        if (pageCache.size() > 0) {
            this.currentPage = pageCache.pop();
            stage.setScene(this.currentPage.getScene());
            if (stage.isMaximized()) {
                stage.setMaximized(false);
                stage.setMaximized(true);
            } else
                stage.setHeight(stage.getHeight()+1);
            stage.show();
        } else {
            throw new PageCacheException("No more cached pages");
        }
    }

    public static StreamingService getInstance() {
        return service;
    }

    public static StreamingService create(Stage stage, int cacheSize) {
        service = new StreamingService(stage, cacheSize);
        return service;
    }

}
