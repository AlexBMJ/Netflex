package database;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class ContentDatabase implements Database {

    public void createNewDatabase(String fileName){

        try {
            Path path = Paths.get("C:/sqlite/db/");
            Files.createDirectories(path);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }

        String url = "jdbc:sqlite:C:/sqlite/db/" + fileName;
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(url)) {
                if (conn != null) {
                    DatabaseMetaData meta = conn.getMetaData();
                    System.out.println("The driver name is " + meta.getDriverName());

                    String sqlMovies = "CREATE TABLE MOVIES(ID INTEGER PRIMARY KEY AUTOINCREMENT, SCORE STRING, WRITER STRING, YEAR STRING, LENGTH STRING, STARS STRING, DIRECTOR STRING, SUMMARY STRING, TITLE STRING, GENRE STRING, TRAILER STRING)";
                    String sqlSeries = "CREATE TABLE SERIES";
                    String sqlEpisodes = "CREATE TABLE EPISODES";

                    Statement stmt = conn.createStatement();
                    stmt.execute(sqlMovies);
                    stmt.execute(sqlSeries);
                    stmt.execute(sqlEpisodes);
                }

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }

    public void connect(){

    }

    public void parseInfoToDatabase() throws IOException, ParseException {
        JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader("movies.json"));

        for(Iterator iterator = jo.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            Map movieID = (Map) jo.get(key);

            ArrayList<Map.Entry> pairList = new ArrayList();
            Iterator<Map.Entry> itr1 = movieID.entrySet().iterator();
            while (itr1.hasNext()) {
            Map.Entry pair = itr1.next();
            pairList.add(pair);
            }
            System.out.println(pairList);

        }


    }


}
