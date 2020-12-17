package database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import content.MovieContent;
import content.SeriesContent;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchDatabase {

    private static SearchDatabase db;
    public Connection conn;

    private SearchDatabase() {
        String url = "jdbc:sqlite:Netflex.db";
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ArrayList search(String searchTerm, String type, HashMap<String, String> searchFilters) {
        String sql = String.format("SELECT * FROM %s WHERE (Title LIKE ?", type);
        for (int i=0;i<searchFilters.keySet().size();i++)
            sql = String.format("%s AND %s LIKE %s", sql, "?", "?");
        sql = String.format("%s) LIMIT 0, 50", sql);

        try (PreparedStatement pstmt  = SearchDatabase.db.conn.prepareStatement(sql)){
            pstmt.setString(1,String.format("%%%s%%",searchTerm));
            int paramIndex = 2;
            for (Map.Entry<String, String> filter : searchFilters.entrySet()) {
                pstmt.setString(paramIndex, filter.getKey());
                pstmt.setString(paramIndex+1, String.format("%%%s%%",filter.getValue()));
                paramIndex++;
            }
            ResultSet results = pstmt.executeQuery();
            ArrayList contentList = new ArrayList();
            if (type == "Movies") {
                try {
                    while (results.next()) {
                        ObjectMapper mapper = new ObjectMapper();
                        MovieContent movie = new MovieContent(
                                results.getString("MovieID"),
                                results.getString("Title"),
                                results.getString("Summary"),
                                results.getString("Length"),
                                Float.parseFloat(results.getString("Score").replace(',', '.')),
                                Integer.parseInt(results.getString("Year")),
                                results.getString("Genre").split(", "),
                                mapper.readValue(results.getString("Writers"), String[].class),
                                mapper.readValue(results.getString("Stars"), String[].class),
                                results.getBytes("IMAGE"),
                                results.getString("Trailer")
                        );
                        contentList.add(movie);
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else if (type == "Shows") {
                Pattern regx = Pattern.compile("(\\d\\d\\d\\d)");

                try {
                    while (results.next()) {
                        ObjectMapper mapper = new ObjectMapper();
                        Matcher m = regx.matcher(results.getString("Year"));
                        SeriesContent show = new SeriesContent(
                                results.getString("ShowID"),
                                results.getString("Title"),
                                results.getString("Summary"),
                                results.getString("Length"),
                                Float.parseFloat(results.getString("Score").replace(',', '.')),
                                (m.find() ? Integer.parseInt(m.group(0)) : 0),
                                (m.find() ? Integer.parseInt(m.group(0)) : 0),
                                results.getString("Genre").split(", "),
                                mapper.readValue((results.getString("Writers") == null ? "[]" : results.getString("Writers")), String[].class),
                                mapper.readValue((results.getString("Stars") == null ? "[]" : results.getString("Stars")), String[].class),
                                results.getBytes("IMAGE"),
                                mapper.readValue(results.getString("Seasons"), TreeMap.class)
                        );
                        contentList.add(show);

                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else if (type == "Episodes") {

            } else return null;
            return contentList;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static SearchDatabase connect() {
        // SQLite SearchDatabase Singleton
        if (db == null) {
            db = new SearchDatabase();
        }
        return db;
    }
}
