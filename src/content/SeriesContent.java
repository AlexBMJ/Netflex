package content;

import java.awt.image.BufferedImage;

public class SeriesContent extends LocalContent {
    private String[][] seasons;
    private int yearEnded;

    public SeriesContent(String id, String title, String summary, String length, float score, int yearAired, int yearEnded, String[] genres, String[] writers, String[] stars, byte[] image, String[][] seasons) {
        super(id, title, summary, length, score, yearAired, genres, writers, stars, image);
        this.seasons = seasons;
    }

    public String[][] getSeasons() {return seasons;}

}
