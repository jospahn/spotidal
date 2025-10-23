package spotidal.service;

import spotidal.dao.TrackDAO;
import spotidal.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LocalDataService {
    private final static Logger log = LoggerFactory.getLogger(LocalDataService.class);

    private final TrackDAO trackDAO;

    public LocalDataService(TrackDAO trackDAO) {
        this.trackDAO = trackDAO;
        trackDAO.createTable();
    }

    public void insertAllTracks(List<Track> tracks) {
        trackDAO.addAllTracks(tracks);
    }

//    public void importTrackJson(Path jsonPath) {
//        var importWithArtistsJson = """
//                    INSERT INTO track (name, artists, album, isrc, durationMs)
//                    SELECT name, to_json(artists), album, isrc, durationMs
//                    FROM (
//                        SELECT *,
//                               ROW_NUMBER() OVER (PARTITION BY isrc ORDER BY name) AS rn
//                        FROM read_json_auto('spotify_tracks.json')
//                    )
//                    WHERE isrc IS NOT NULL AND rn = 1
//                    ON CONFLICT DO NOTHING;
//                """;
//
//        try (Connection conn = DriverManager.getConnection(DB_URL);
//             Statement stmt = conn.createStatement()
//        ) {
//            stmt.execute(importWithArtistsJson);
//            log.info("JSON import completed with deduplication");
//        } catch (SQLException e) {
//            log.error("Error importing json", e);
//        }
//    }

}
