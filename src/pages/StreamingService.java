package pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import content.LocalContent;
import database.SearchDatabase;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class StreamingService {
    private static StreamingService service;
    private static SearchDatabase database = new SearchDatabase();
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
        stage.show();
    }

    public void prevPage() throws PageCacheException {
        if (pageCache.size() > 0) {
            this.currentPage = pageCache.pop();
            stage.setScene(this.currentPage.getScene());
            stage.show();
        } else {
            throw new PageCacheException("No more pages");
        }
    }

    public ArrayList<LocalContent> search(String searchTerm, String contentType, HashMap<String, String> searchFilters) {
        try {
            String table;
            boolean precision;
            if (contentType == "Episodes") {
                table = contentType;
                precision = true;
            } else {
                table = (contentType == "Movie" ? "Movies" : "Shows");
                precision = false;
            }

            return database.search(searchTerm, table, searchFilters, precision);
        } catch (SQLException | JsonProcessingException throwables) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error! " + throwables.getMessage());
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> alert.close());
            return new ArrayList() {};
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
