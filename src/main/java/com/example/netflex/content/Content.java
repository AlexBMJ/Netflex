package com.example.netflex.content;

import javafx.scene.image.Image;

public interface Content {
    String getId();
    String getTitle();
    String getSummary();
    String getLength();
    float getScore();
    int getYear();
    String[] getGenres();
    String[] getWriters();
    String[] getStars();
    Image getImage();

}
