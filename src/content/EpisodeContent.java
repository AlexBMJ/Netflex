package content;

import java.awt.image.BufferedImage;

public class EpisodeContent extends LocalContent {
    private int episodeNumber;
    private int seasonNumber;
    private String showId;

    public EpisodeContent(String id, String title, String summary, String length, int score, int year, String[] genres, String[] writers, String[] stars, byte[] image, int episodeNumber, int seasonNumber) {
        super(id, title, summary, length, score, year, genres, writers, stars, image);
        this.episodeNumber = episodeNumber;
        this.seasonNumber = seasonNumber;
    }

    public int getEpisodeNumber() {return episodeNumber;}

    public int getSeasonNumber() {return seasonNumber;}

    public String getShowId() {return showId;}
}
