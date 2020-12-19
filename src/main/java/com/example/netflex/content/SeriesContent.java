package com.example.netflex.content;

import java.util.*;

public class SeriesContent extends LocalContent {
    private int yearEnded;
    private ArrayList<String[]> seasons;

    public SeriesContent(String id, String title, String summary, String length, float score, int yearAired, int yearEnded, String[] genres, String[] writers, String[] stars, byte[] image, TreeMap<String,ArrayList<String>> seasons) {
        super(id, title, summary, length, score, yearAired, genres, writers, stars, image);
        this.yearEnded = yearEnded;
        this.seasons = new ArrayList();
        seasons.values().forEach(s -> this.seasons.add(s.toArray(new String[s.size()])));
    }

    public ArrayList<String[]> getSeasons() {return seasons;}
    public int getYearEnded() {return yearEnded;}

}
