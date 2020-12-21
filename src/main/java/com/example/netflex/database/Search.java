package com.example.netflex.database;

import com.example.netflex.content.Content;
import com.example.netflex.content.ExternalContent;
import com.example.netflex.content.TVDBResult;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Search {
    private ArrayList<Runnable> onCompleted;
    private ArrayList<Runnable> onFailed;
    private ArrayList<Content> result;
    private String errorMsg;
    private Thread searchThread;
    private String contentType;
    private HashMap<String, String> searchFilters;

    public Search(String contentType, HashMap<String, String> searchFilters) {
        this.contentType = contentType;
        this.searchFilters = searchFilters;
        this.onCompleted = new ArrayList();
        this.onFailed = new ArrayList();
    }

    public void addOnCompleted(Runnable after) { onCompleted.add(after); }
    public void addOnFailed(Runnable failed) { onFailed.add(failed); }

    public void run() {
        searchThread = new Thread(() -> {
            try {
                if (searchFilters.containsKey("API Search"))
                    result = APISearch(contentType, searchFilters);
                else
                    result = databaseSearch(contentType, searchFilters);
            } catch (SQLException | JsonProcessingException throwables) {
                errorMsg = throwables.getMessage();
                for (Runnable fail : onFailed)
                    fail.run();
                return;
            }
            for (Runnable complete : onCompleted)
                complete.run();
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

    public static ArrayList<Content> APISearch(String contentType, HashMap<String, String> searchFilters) {
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

    public static ArrayList<Content> databaseSearch(String contentType, HashMap<String, String> searchFilters) throws SQLException, JsonProcessingException {
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
        return ContentDatabase.getInstance().search(contentType, parsedFilters, precision);
    }
}
