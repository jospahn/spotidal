package model;

import java.util.List;

public class Track {
    private String name;
    private List<String> artists;
    private String album;
    private String isrc; // Wichtig für Matching zwischen Plattformen!
    private int durationMs;

    // Getters und Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getArtists() { return artists; }
    public void setArtists(List<String> artists) { this.artists = artists; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getIsrc() { return isrc; }
    public void setIsrc(String isrc) { this.isrc = isrc; }

    public int getDurationMs() { return durationMs; }
    public void setDurationMs(int durationMs) { this.durationMs = durationMs; }
}
