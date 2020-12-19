package com.example.netflex.content;

public class EpisodeContent extends LocalContent {
    private int episodeNumber;
    private int seasonNumber;
    private String showId;

    public EpisodeContent(String id, String title, String summary, String length, String[] writers, String[] stars, byte[] image, String showId, int episodeNumber, int seasonNumber) {
        super(id, title, summary, length, 0f, 0, new String[]{}, writers, stars, image);
        this.showId = showId;
        this.episodeNumber = episodeNumber;
        this.seasonNumber = seasonNumber;
    }

    public int getEpisodeNumber() {return episodeNumber;}

    public int getSeasonNumber() {return seasonNumber;}

    public String getShowId() {return showId;}
}
