package content;

import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;

public abstract class LocalContent implements Content {
    protected String id;
    protected String title;
    protected String summary;
    protected String length;
    protected float score;
    protected int year;
    protected String[] genres;
    protected String[] writers;
    protected String[] stars;
    protected byte[] image;

    public LocalContent(String id, String title, String summary, String length, float score, int year, String[] genres, String[] writers, String[] stars, byte[] image) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.length = length;
        this.score = score;
        this.year = year;
        this.genres = genres;
        this.writers = writers;
        this.stars = stars;
        this.image = image;
    }


    public String getId() {return id;}

    public String getTitle() {return title;}

    public String getSummary() {return summary;}

    public String getLength() {return length;}

    public float getScore() {return score;}

    public int getYear() {return year;}

    public String[] getGenres() {return genres;}

    public String[] getWriters() {return writers;}

    public String[] getStars() {return stars;}

    public Image getImage() {return new Image(new ByteArrayInputStream(this.image));}

}
