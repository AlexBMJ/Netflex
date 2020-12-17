package database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

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

    public static ResultSet search(String searchTerm, String type, HashMap<String, String> searchFilters) {
        String sql = String.format("SELECT * FROM %s WHERE (Title LIKE ?", type);
        for (int i=0;i<searchFilters.keySet().size();i++)
            sql = String.format("%s AND %s LIKE %%%s%%", sql, "?", "?");
        sql = String.format("%s) LIMIT 0, 50", sql);

        try (PreparedStatement pstmt  = SearchDatabase.db.conn.prepareStatement(sql)){
            pstmt.setString(1,searchTerm);
            int paramIndex = 2;
            System.out.println(searchFilters.toString());
            for (Map.Entry<String, String> filter : searchFilters.entrySet()) {
                pstmt.setString(paramIndex, filter.getKey());
                pstmt.setString(paramIndex+1, filter.getValue());
                paramIndex++;
            }
            ResultSet rs = pstmt.executeQuery();
            return rs;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static SearchDatabase connect() {
        // SQLite connection string
        if (db == null) {
            db = new SearchDatabase();
        }
        return db;
    }
}
