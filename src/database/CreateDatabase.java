package database;

public class CreateDatabase {

    public static void main(String[] args)
    {
        // I ALREADY GENERATED THE FILE NO NEED TO RE-RUN!!!
        System.out.println("Database has already been generated!");
        System.exit(0);
        ContentDatabase cd = new ContentDatabase();
        cd.createNewDatabase("Netflex.db");
        try {
            cd.parseInfoToDatabase("movies", "posters/");
            cd.parseInfoToDatabase("shows", "posters/");
            cd.parseInfoToDatabase("episodes", "thumbnails/");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
