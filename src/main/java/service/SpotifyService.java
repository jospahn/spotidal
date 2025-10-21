package service;

import auth.SpotifyAuthService;
import model.*;
import model.Playlist;
import model.Track;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.library.GetUsersSavedTracksRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpotifyService {

    private final SpotifyApi spotifyApi;
    private final Gson gson;

    public SpotifyService() {
        var auth = new SpotifyAuthService();
        var token = auth.checkAndRefresh().orElseThrow();
        this.spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(token.accessToken())
                .build();

        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public String getCurrentUserId() throws Exception {
        User user = spotifyApi.getCurrentUsersProfile().build().execute();
        return user.getId();
    }

    public void exportLikedSongs(String outputFile) throws Exception {
        System.out.println("Lade Liked Songs...");

        List<Track> tracks = new ArrayList<>();
        int offset = 0;
        int limit = 50;

        while (true) {
            GetUsersSavedTracksRequest request = spotifyApi
                    .getUsersSavedTracks()
                    .limit(limit)
                    .offset(offset)
                    .build();

            Paging<SavedTrack> savedTracks = request.execute();

            if (savedTracks.getItems().length == 0) break;

            for (SavedTrack savedTrack : savedTracks.getItems()) {
                tracks.add(convertTrack(savedTrack.getTrack()));
            }

            System.out.println("  " + tracks.size() + " Songs geladen...");

            if (savedTracks.getNext() == null) break;
            offset += limit;
        }

        ExportData exportData = new ExportData();
        exportData.setLikedSongs(tracks);

        saveToFile(exportData, outputFile);
        System.out.println("✓ " + tracks.size() + " Liked Songs exportiert");
    }

    public void exportPlaylists(String outputFile) throws Exception {
        System.out.println("Lade Playlists...");

        List<Playlist> playlists = new ArrayList<>();
        int offset = 0;
        int limit = 50;

        while (true) {
            GetListOfCurrentUsersPlaylistsRequest request = spotifyApi
                    .getListOfCurrentUsersPlaylists()
                    .limit(limit)
                    .offset(offset)
                    .build();

            Paging<PlaylistSimplified> playlistPage = request.execute();

            if (playlistPage.getItems().length == 0) break;

            for (PlaylistSimplified pl : playlistPage.getItems()) {
                playlists.add(loadFullPlaylist(pl.getId()));
                System.out.println("  Playlist geladen: " + pl.getName());
            }

            if (playlistPage.getNext() == null) break;
            offset += limit;
        }

        ExportData exportData = new ExportData();
        exportData.setPlaylists(playlists);

        saveToFile(exportData, outputFile);
        System.out.println("✓ " + playlists.size() + " Playlists exportiert");
    }

    public void exportAll(String outputFile) throws Exception {
        System.out.println("Exportiere alle Daten...\n");

        ExportData exportData = new ExportData();

        // Liked Songs
        System.out.println("1. Liked Songs:");
        List<Track> tracks = loadAllLikedSongs();
        exportData.setLikedSongs(tracks);

        // Playlists
        System.out.println("\n2. Playlists:");
        List<Playlist> playlists = loadAllPlaylists();
        exportData.setPlaylists(playlists);

        saveToFile(exportData, outputFile);
    }

    public List<Track> loadAllLikedSongs() throws Exception {
        List<Track> tracks = new ArrayList<>();
        int offset = 0;
        int limit = 50;

        while (true) {
            GetUsersSavedTracksRequest request = spotifyApi
                    .getUsersSavedTracks()
                    .limit(limit)
                    .offset(offset)
                    .build();

            Paging<SavedTrack> savedTracks = request.execute();

            if (savedTracks.getItems().length == 0) break;

            for (SavedTrack savedTrack : savedTracks.getItems()) {
                tracks.add(convertTrack(savedTrack.getTrack()));
            }

            System.out.println("  " + tracks.size() + " Songs...");

            if (savedTracks.getNext() == null) break;
            offset += limit;
        }

        System.out.println("  ✓ " + tracks.size() + " Liked Songs");
        return tracks;
    }

    public List<Playlist> loadAllPlaylists() throws Exception {
        List<Playlist> playlists = new ArrayList<>();
        int offset = 0;
        int limit = 50;

        while (true) {
            GetListOfCurrentUsersPlaylistsRequest request = spotifyApi
                    .getListOfCurrentUsersPlaylists()
                    .limit(limit)
                    .offset(offset)
                    .build();

            Paging<PlaylistSimplified> playlistPage = request.execute();

            if (playlistPage.getItems().length == 0) break;

            for (PlaylistSimplified pl : playlistPage.getItems()) {
                playlists.add(loadFullPlaylist(pl.getId()));
                System.out.println("  " + pl.getName());
            }

            if (playlistPage.getNext() == null) break;
            offset += limit;
        }

        System.out.println("  ✓ " + playlists.size() + " Playlists");
        return playlists;
    }

    private Playlist loadFullPlaylist(String playlistId) throws Exception {
        se.michaelthelin.spotify.model_objects.specification.Playlist spotifyPlaylist =
                spotifyApi.getPlaylist(playlistId).build().execute();

        List<Track> tracks = new ArrayList<>();
        for (PlaylistTrack pt : spotifyPlaylist.getTracks().getItems()) {
            if (pt.getTrack() instanceof se.michaelthelin.spotify.model_objects.specification.Track) {
                tracks.add(convertTrack((se.michaelthelin.spotify.model_objects.specification.Track) pt.getTrack()));
            }
        }

        return new Playlist(
                spotifyPlaylist.getName(),
                spotifyPlaylist.getDescription(),
                spotifyPlaylist.getIsPublicAccess(),
                tracks
        );
    }

    private Track convertTrack(se.michaelthelin.spotify.model_objects.specification.Track spotifyTrack) {

        List<String> artists = new ArrayList<>();
        for (ArtistSimplified artist : spotifyTrack.getArtists()) {
            artists.add(artist.getName());
        }

        return new Track(
                spotifyTrack.getName(),
                artists,
                spotifyTrack.getAlbum().getName(),
                spotifyTrack.getExternalIds().getExternalIds().get("isrc"),
                spotifyTrack.getDurationMs()
        );

    }

    private void saveToFile(ExportData data, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(data, writer);
        }
    }
}