package model;

import java.util.List;

public record Playlist (
        String name,
        String description,
        boolean isPublic,
        List<Track> tracks
) {

}
