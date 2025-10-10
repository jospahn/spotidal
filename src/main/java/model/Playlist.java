package model;

import java.util.List;

public class Playlist {
    private String name;
    private String description;
    private boolean isPublic;
    private List<Track> tracks;

    // Getters und Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public List<Track> getTracks() { return tracks; }
    public void setTracks(List<Track> tracks) { this.tracks = tracks; }
}
