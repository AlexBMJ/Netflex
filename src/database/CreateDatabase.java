package database;

public class CreateDatabase {

    public static void main(String[] args)
    {
        ContentDatabase cd = new ContentDatabase();
        cd.createNewDatabase("Netflex.db");
        try {
            cd.parseInfoToDatabase("movies");
            cd.parseInfoToDatabase("shows");
            cd.parseInfoToDatabase("episodes");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
