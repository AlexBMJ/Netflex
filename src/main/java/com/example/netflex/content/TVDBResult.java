package com.example.netflex.content;

import java.util.Map;

public class TVDBResult {
    private String type;
    private String name;
    private String url;
    private int id;
    private String image;
    private String poster;
    private String banner;
    private String slug;
    private String status;
    private String network;
    private String released;
    private String first_aired;
    private Map<String, String> translations;
    private Map<String, String> overviews;

    public String getType() { return type; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public int getId() { return id; }
    public String getImage() { return image; }
    public String getPoster() { return poster; }
    public String getBanner() { return banner; }
    public String getStatus() { return status; }
    public String getReleased() { return released; }
    public String getFirstAired() { return first_aired; }
    public Map<String, String> getTranslations() { return translations; }
    public Map<String, String> getOverviews() { return overviews; }

}

