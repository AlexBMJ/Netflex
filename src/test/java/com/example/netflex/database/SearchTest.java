package com.example.netflex.database;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SearchTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void getResult() {
        String contentType = "Movies";
        LinkedHashMap<String, String> parsedFilters = new LinkedHashMap();
        parsedFilters.put("Title","No Movie");
        Search search = new Search(contentType, parsedFilters);
        search.addOnCompleted(() -> {assertNotNull(search.getResult());});
    }

    @Test
    public void getError() {
        String contentType = "Movies";
        LinkedHashMap<String, String> parsedFilters = new LinkedHashMap();
        parsedFilters.put("Title","No Movie");
        Search search = new Search(contentType, parsedFilters);
        search.addOnFailed(() -> {assertNotNull(search.getError());});
    }

    @Test
    public void getResultNull() {
        String contentType = "Movies";
        LinkedHashMap<String, String> parsedFilters = new LinkedHashMap();
        parsedFilters.put("Title","No Movie");
        Search search = new Search(contentType, parsedFilters);
        assertEquals(search.getResult(), null);
    }

    @Test
    public void getErrorNull() {
        String contentType = "Movies";
        LinkedHashMap<String, String> parsedFilters = new LinkedHashMap();
        parsedFilters.put("Title","No Movie");
        Search search = new Search(contentType, parsedFilters);
        assertEquals(search.getError(), null);
    }
}