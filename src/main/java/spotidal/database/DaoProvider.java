package spotidal.database;

import spotidal.dao.IsrcTidalIdDAO;
import spotidal.dao.MusicAccountDAO;
import spotidal.dao.TrackDAO;
import spotidal.dao.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DaoProvider {
    private static final Logger log = LoggerFactory.getLogger(DaoProvider.class);
    private static final String DB_DRIVER = "jdbc:duckdb:";

    private final Connection conn;

    public DaoProvider(String dbPath) throws SQLException {
        this.conn = DriverManager.getConnection(DB_DRIVER + dbPath);
    }

    public TrackDAO getTrackDAO() {
        return new TrackDAOImpl(conn);
    }

    public IsrcTidalIdDAO getIsrcTidalIdDAO() {
        return new IsrcTidalIdDAOImpl(conn);
    }

    public UserDAO getUserDAO() {
        return new UserDAOImpl(conn);
    }

    public MusicAccountDAO getMusicAccountDAO() {
        return new MusicAccountDAOImpl(conn);
    }




//    public void importJson() {
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
