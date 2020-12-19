package database;

import com.fasterxml.jackson.core.JsonProcessingException;
import content.Content;
import pages.StreamingService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Search {
    private Runnable onCompleted;
    private Runnable onFailed;
    private ArrayList<Content> result;
    private String errorMsg;
    private Thread searchThread;

    private String contentType;
    private HashMap<String, String> searchFilters;

    public Search(String contentType, HashMap<String, String> searchFilters) {
        this.contentType = contentType;
        this.searchFilters = searchFilters;
    }

    public void setOnCompleted(Runnable after) {
        onCompleted = after;
    }
    public void setOnFailed(Runnable failed) { onFailed = failed; }

    public void run() {
        searchThread = new Thread(() -> {
            try {
                if (searchFilters.containsKey("API Search"))
                    result = StreamingService.getInstance().APISearch(contentType, searchFilters);
                else
                    result = StreamingService.getInstance().databaseSearch(contentType, searchFilters);
            } catch (SQLException | JsonProcessingException throwables) {
                errorMsg = throwables.getMessage();
                onFailed.run();
                return;
            }
            onCompleted.run();
        });
        searchThread.setDaemon(true);
        searchThread.start();
    }

    public ArrayList<Content> getResult() {
        return result;
    }

    public String getError() {
        return errorMsg;
    }
}
