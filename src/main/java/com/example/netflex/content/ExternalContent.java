package com.example.netflex.content;

import javafx.scene.image.Image;

public class ExternalContent implements Content {
    protected String id;
    protected String title;
    protected String summary;
    protected int year;
    protected String imageUrl;

    public ExternalContent(TVDBResult tvdb) {
        this.id = String.valueOf(tvdb.getId());
        this.title = tvdb.getName();
        this.summary = tvdb.getOverviews().get("eng");
        if (tvdb.getReleased() != null && tvdb.getReleased().length() > 0)
            this.year = Integer.parseInt(tvdb.getReleased());
        else if (tvdb.getFirstAired() != null && tvdb.getFirstAired().length() > 0)
            this.year = Integer.parseInt(tvdb.getFirstAired());
        this.imageUrl = tvdb.getImage();
    }

    public String getId() {return null;}

    public String getLength() {return null;}

    public float getScore() {return -1;}

    public String[] getGenres() {return new String[]{};}

    public String[] getWriters() {return new String[]{};}

    public String[] getStars() {return new String[]{};}

    public String getTitle() {return title;}

    public String getSummary() {return summary;}

    public int getYear() {return year;}

    public Image getImage() {
        return new Image(imageUrl);
    }
}
