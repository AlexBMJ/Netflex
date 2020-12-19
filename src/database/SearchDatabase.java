package database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import content.EpisodeContent;
import content.MovieContent;
import content.SeriesContent;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SearchDatabase {
    public Connection conn;

    public SearchDatabase() {
        String url = "jdbc:sqlite:Netflex.db";
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList search(String table, HashMap<String, String> searchFilters, boolean precise) throws SQLException, JsonProcessingException{
        String sql = String.format("SELECT * FROM %s", table);

        TreeMap<String, String> orderedMap = new TreeMap();
        orderedMap.putAll(searchFilters);

        ArrayList<String> sqlFilter = new ArrayList();
        for (String key : orderedMap.keySet())
            sqlFilter.add(key + " LIKE ?");
        if (sqlFilter.size() > 0)
            sql += String.format(" WHERE (%s)", sqlFilter.stream().collect(Collectors.joining(" AND ")));
        if (precise)
            sql += " ORDER BY EpisodeNumber ASC";
        else
            sql += " LIMIT 0, 50";

        try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {
            for (int i=0;i<orderedMap.size();i++)
                pstmt.setString(i+1, String.format("%2$s%1$s%2$s", orderedMap.get(sqlFilter.get(i).replaceAll(" LIKE \\?", "")),(precise ? "" : "%")));

            ResultSet results = pstmt.executeQuery();
            ArrayList contentList = new ArrayList();
            if (table == "Movies") {
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
            } else if (table == "Shows") {
                Pattern regx = Pattern.compile("(\\d\\d\\d\\d)");
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
            } else if (table == "Episodes") {
                while (results.next()) {
                    ObjectMapper mapper = new ObjectMapper();
                    EpisodeContent episode = new EpisodeContent(
                            results.getString("EpisodeID"),
                            results.getString("Title"),
                            results.getString("Summary"),
                            results.getString("Length"),
                            mapper.readValue((results.getString("Writers") == null ? "[]" : results.getString("Writers")), String[].class),
                            mapper.readValue((results.getString("Stars") == null ? "[]" : results.getString("Stars")), String[].class),
                            results.getBytes("IMAGE"),
                            results.getString("ShowID"),
                            Integer.parseInt(results.getString("EpisodeNumber")),
                            Integer.parseInt(results.getString("Season"))
                    );
                    contentList.add(episode);
                }
            }
            return contentList;
        }
    }
}
