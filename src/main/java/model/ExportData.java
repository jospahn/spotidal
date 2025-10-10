package model;

import java.util.List;

public class ExportData {
    private List<Track> likedSongs;
    private List<Playlist> playlists;

    // Getters und Setters
    public List<Track> getLikedSongs() { return likedSongs; }
    public void setLikedSongs(List<Track> likedSongs) { this.likedSongs = likedSongs; }

    public List<Playlist> getPlaylists() { return playlists; }
    public void setPlaylists(List<Playlist> playlists) { this.playlists = playlists; }
}
