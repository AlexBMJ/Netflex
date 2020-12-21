package com.example.netflex.database;

import com.example.netflex.content.Content;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

public class ContentDatabaseTest {

    @Test
    public void search() throws SQLException, JsonProcessingException {
        String contentType = "Movies";
        LinkedHashMap<String, String> parsedFilters = new LinkedHashMap();
        parsedFilters.put("Title","No Movie");
        ArrayList<Content> content = ContentDatabase.getInstance().search(contentType, parsedFilters, false);

        assertEquals(content, new ArrayList<Content>());
    }
}