package content;

import java.awt.image.BufferedImage;

public class MovieContent extends LocalContent {
    private String trailer;

    public MovieContent(String id, String title, String summary, String length, float score, int year, String[] genres, String[] writers, String[] stars, byte[] image, String trailer) {
        super(id, title, summary, length, score, year, genres, writers, stars, image);
        this.trailer = trailer;
    }

    public String getTrailer() {return trailer;}
}
