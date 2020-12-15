package database;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.sql.rowset.serial.SerialArray;
import java.sql.*;

public class db {

    private String[] movieFields = new String[]{"MovieID","Title","Director","Writers","Stars","Summary","Length","Year","Genre","Score","Trailer"};
    final private String url = "jdbc:sqlite:test.db";

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void addMovie(JSONObject movieJson) {
        String sql = "INSERT INTO Movies(MovieID,Title,Director,Writers,Stars,Summary,Length,Year,Genre,Score,Trailer) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1,movieJson.get("MovieID").toString());
            pstmt.setString(2,movieJson.get("Title").toString());
            pstmt.setArray(9,conn.createArrayOf("VARCHAR", new String[]{"ok","ok2"}));
            JSONObject people = (JSONObject)(movieJson.get("People"));
            if (!people.containsKey("Director"))
                pstmt.setArray(3, conn.createArrayOf("text", ((JSONArray) people.get("Director")).toArray()));
            else
                pstmt.setNull(3,Types.ARRAY);
            if (!people.containsKey("Writers"))
                pstmt.setArray(4, conn.createArrayOf("text", ((JSONArray) people.get("Writers")).toArray()));
            else
                pstmt.setNull(4,Types.ARRAY);
            if (!people.containsKey("Stars"))
                pstmt.setArray(5, conn.createArrayOf("text", ((JSONArray) people.get("Stars")).toArray()));
            else
                pstmt.setNull(5,Types.ARRAY);



            pstmt.setString(6,movieJson.get("Summary").toString());
            pstmt.setString(7,movieJson.get("Length").toString());
            pstmt.setString(8,movieJson.get("Year").toString());
            //pstmt.setArray(9,(Array)movieJson.get("Genre"));
            pstmt.setFloat(10,Float.parseFloat(movieJson.get("Score").toString().replace(',','.')));
            pstmt.setString(11,movieJson.get("Trailer").toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createTable(String name) {
        try (Connection conn = DriverManager.getConnection(url)) {
            Statement stmt = conn.createStatement();
            stmt.execute(String.format("CREATE TABLE IF NOT EXISTS %s (MovieID PRIMARY KEY,Title,Director,Writers,Stars,Summary,Length,Year,Genre,Score,Trailer)", name));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) throws ParseException {

        db app = new db();

        app.createTable("Movies");
        app.addMovie((JSONObject) new JSONParser().parse("{\"MovieID\": \"tt0111161\", \"Title\": \"The Shawshank Redemption\", \"People\": {\"Director\": [\"Frank Darabont\"], \"Writers\": [\"Stephen King\", \"Frank Darabont\"], \"Stars\": [\"Tim Robbins\", \"Morgan Freeman\", \"Bob Gunton\"]}, \"Summary\": \"Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.\", \"Length\": \"2h 22min\", \"Year\": \"1994\", \"Genre\": \"Drama\", \"Score\": \"9,3\", \"Trailer\": \"NmzuHjWmXOc\"}"));
    }
}
