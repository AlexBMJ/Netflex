package database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import content.EpisodeContent;
import content.MovieContent;
import content.SeriesContent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ContentDatabase {
    public Connection conn;

    public ContentDatabase() {
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

    public void createNewDatabase() throws SQLException, IOException, ClassNotFoundException {
        Path path = Paths.get(".");
        Files.createDirectories(path);
        Class.forName("org.sqlite.JDBC");
        if (conn != null) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("The driver name is " + meta.getDriverName());
        }
    }

    public void parseInfoToDatabase(String jsonFile, String imagePath) throws IOException, ParseException, SQLException, ClassNotFoundException {

        JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader(jsonFile + ".json"));
        for (Iterator iterator = jo.keySet().iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            Map movieID = (Map) jo.get(key);

            ArrayList<Map.Entry> pairList = new ArrayList();
            Iterator<Map.Entry> itr1 = movieID.entrySet().iterator();
            while (itr1.hasNext()) {
                Map.Entry pair = itr1.next();
                pairList.add(pair);
            }

            String prepareKey = "";
            String prepareValue = "";
            String prepareDatabase = "";
            byte[] prepareImage;

            ArrayList<String> information = new ArrayList();

            for (Map.Entry pair : pairList) {
                if (pair.getKey().toString().equals("People")) {
                    String s = "{\"" + pair.getKey() + "\": " + pair.getValue() + "}";
                    JSONObject joPeople = (JSONObject) new JSONParser().parse(s);
                    for (Iterator iterator2 = joPeople.keySet().iterator(); iterator2.hasNext(); ) {
                        String key2 = (String) iterator2.next();
                        Map peopleID = (Map) joPeople.get(key2);

                        Iterator<Map.Entry> itr2 = peopleID.entrySet().iterator();
                        while (itr2.hasNext()) {
                            Map.Entry pairPeople = itr2.next();

                            if (pairPeople.getKey().equals("Writer")) {
                                pairPeople = new AbstractMap.SimpleEntry<>("Writers", pairPeople.getValue().toString());
                            } else if (pairPeople.getKey().equals("Directors")) {
                                pairPeople = new AbstractMap.SimpleEntry<>("Director", pairPeople.getValue().toString());
                            } else if (pairPeople.getKey().equals("Creators")) {
                                pairPeople = new AbstractMap.SimpleEntry<>("Creator", pairPeople.getValue().toString());
                            } else if (pairPeople.getKey().equals("Star")) {
                                pairPeople = new AbstractMap.SimpleEntry<>("Stars", pairPeople.getValue().toString());
                            }

                            prepareKey += pairPeople.getKey() + ", ";
                            prepareValue += "?, ";
                            information.add(pairPeople.getValue().toString());
                        }
                    }
                } else {
                    prepareKey += pair.getKey() + ", ";
                    prepareValue += "?, ";
                    information.add(pair.getValue().toString());
                }
                if (pair.getKey().toString().equals("People")) {
                    prepareDatabase += "Writers STRING, Stars STRING, Director STRING, Creator STRING, ";
                } else {
                    prepareDatabase += pair.getKey() + " STRING, ";
                }
            }
            prepareKey = prepareKey.substring(0, prepareKey.length() - 2);
            prepareValue = prepareValue.substring(0, prepareValue.length() - 2);
            try {
                prepareImage = imageToBytes(imagePath + key + ".jpg");
            } catch (IOException e) {
                prepareImage = imageToBytes(imagePath + "missing.jpg");
            }
            prepareDatabase = prepareDatabase.substring(0, prepareDatabase.length() - 2);

            Class.forName("org.sqlite.JDBC");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS " + jsonFile.toUpperCase() + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + prepareDatabase + ", IMAGE)");
                String sql = "INSERT INTO " + jsonFile.toUpperCase() + "(" + prepareKey + ", IMAGE) VALUES(" + prepareValue + ", ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);

                for (int i = 0; i < information.size(); i++) {
                    pstmt.setString(i + 1, information.get(i));
                }
                pstmt.setBytes(information.size() + 1, prepareImage);
                pstmt.executeUpdate();

            }
            System.out.println("Done" + jsonFile);
        }
    }

    public byte[] imageToBytes(String imagePath) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage img = ImageIO.read(new File(imagePath));
        ImageIO.write(img, "jpg", baos);
        return baos.toByteArray();
    }
}
