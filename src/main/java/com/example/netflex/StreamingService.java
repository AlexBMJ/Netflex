package com.example.netflex;

import com.example.netflex.controllers.Controller;
import com.example.netflex.pages.PageCacheException;
import javafx.stage.Stage;

import java.util.LinkedList;

public class StreamingService {
    private static StreamingService service;
    private LinkedList<Controller> controllerCache;
    private Stage stage;
    private int cacheSize;
    private Controller currentController;

    private StreamingService(Stage stage, int cacheSize) {
        this.cacheSize = cacheSize;
        this.stage = stage;
        this.controllerCache = new LinkedList();
    }

    public void addPage(Controller controller) {
        if (controllerCache.size() >= cacheSize) {
            controllerCache.remove(controllerCache.size()-1);
        }
        if (this.currentController != null)
            controllerCache.add(0, this.currentController);
        this.currentController = controller;
        stage.setScene(this.currentController.getPage().getScene());
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            stage.setMaximized(true);
        } else
            stage.setHeight(stage.getHeight()-1);
        stage.show();
    }

    public void prevPage() throws PageCacheException {
        if (controllerCache.size() > 0) {
            this.currentController = controllerCache.pop();
            stage.setScene(this.currentController.getPage().getScene());
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
