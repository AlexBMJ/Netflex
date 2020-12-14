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
import java.sql.*;
import java.util.*;

public class ContentDatabase implements Database {

    private String url;

    public void createNewDatabase(String fileName){

        try {
            Path path = Paths.get("C:/sqlite/db/");
            Files.createDirectories(path);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }

        String url = "jdbc:sqlite:C:/sqlite/db/" + fileName;
        this.url = url;
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(url)) {
                if (conn != null) {
                    DatabaseMetaData meta = conn.getMetaData();
                    System.out.println("The driver name is " + meta.getDriverName());
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

    public void parseInfoToDatabase(String jsonFile) throws IOException, ParseException {

        JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader(jsonFile + ".json"));

        for(Iterator iterator = jo.keySet().iterator(); iterator.hasNext();) {
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
            for (Map.Entry pair : pairList) {
                pair.setValue(pair.getValue().toString().replace("'","£"));

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
                            prepareValue += "'" + pairPeople.getValue() + "', ";
                        }
                    }
                } else {
                    prepareKey += pair.getKey() + ", ";
                    prepareValue += "'" + pair.getValue() + "', ";
                }
                    if (pair.getKey().toString().equals("People")) {
                        prepareDatabase += "Writers STRING, Stars STRING, Director STRING, Creator STRING, ";
                    } else {
                        prepareDatabase += pair.getKey() + " STRING, ";
                    }
            }
            prepareKey = prepareKey.substring(0, prepareKey.length()-2);
            prepareValue = prepareValue.substring(0, prepareValue.length()-2);
            prepareDatabase = prepareDatabase.substring(0, prepareDatabase.length()-2);

            try {
                Class.forName("org.sqlite.JDBC");
                try (Connection conn = DriverManager.getConnection(url)) {
                    Statement stmt = conn.createStatement();
                    stmt.execute("CREATE TABLE IF NOT EXISTS " + jsonFile.toUpperCase() + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + prepareDatabase + ")");
                    stmt.executeUpdate("INSERT INTO " + jsonFile.toUpperCase() + "(" + prepareKey + ") VALUES(" + prepareValue + ")");

                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }

        }
        System.out.println("Done");
    }
}
