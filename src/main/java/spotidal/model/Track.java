package spotidal.model;

import java.util.List;

public record Track(
        String name,
        List<String> artists,
        String album,
        String isrc,
        int durationMs
) {}
