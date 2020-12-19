package pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import content.Content;
import content.ExternalContent;
import content.TVDBResult;
import database.AlgoliaAPI;
import database.ContentDatabase;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;

public class StreamingService {
    private static StreamingService service;
    private static ContentDatabase database = new ContentDatabase();
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
            throw new PageCacheException("No more pages");
        }
    }

    public ArrayList<Content> APISearch(String contentType, HashMap<String, String> searchFilters) {
        String type = AlgoliaAPI.MOVIE;
        if (contentType == "Movies")
            type = AlgoliaAPI.MOVIE;
        else if (contentType == "Shows")
            type = AlgoliaAPI.TVSHOW;

        String query = searchFilters.get("API Search");
        if (query.length() < 1)
            query = "mission";

        ArrayList<Content> content = new ArrayList();
        List<TVDBResult> hits = AlgoliaAPI.getSeriesHits(query, type, 30);
        for (TVDBResult result : hits)
            if (AlgoliaAPI.isValid(result))
                content.add(new ExternalContent(result));
        return content;
    }

    public ArrayList<Content> databaseSearch(String contentType, HashMap<String, String> searchFilters) throws SQLException, JsonProcessingException {
        boolean precision = false;
        if (contentType == "Episodes")
            precision = true;
        HashMap parsedFilters = new HashMap();
        for (Map.Entry<String, String> entry : searchFilters.entrySet()) {
            if (entry.getKey() == "Year")
                parsedFilters.put(entry.getKey(), String.valueOf(Integer.parseInt(entry.getValue()) / 10));
            else if (entry.getKey() == "Score")
                parsedFilters.put(entry.getKey(), entry.getValue() + ",");
            else
                parsedFilters.put(entry.getKey(), entry.getValue());
        }
        return database.search(contentType, parsedFilters, precision);
    }

    public static StreamingService getInstance() {
        return service;
    }

    public static StreamingService create(Stage stage, int cacheSize) {
        service = new StreamingService(stage, cacheSize);
        return service;
    }

}
