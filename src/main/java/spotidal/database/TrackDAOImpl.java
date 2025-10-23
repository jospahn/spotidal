package spotidal.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import spotidal.dao.DaoException;
import spotidal.dao.TrackDAO;
import spotidal.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrackDAOImpl extends AbstractDatabaseDAO implements TrackDAO {
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<String>>() {}.getType();

    public TrackDAOImpl(Connection conn) {
        super(conn);
    }

    @Override
    public void insert(Track track)  {
        String sql = "INSERT INTO track (name, artists, album, isrc, durationMs) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, track.name());
            var artistsJson = gson.toJson(track.artists());
            stmt.setString(2, artistsJson);
            stmt.setString(3, track.album());
            stmt.setString(4, track.isrc());
            stmt.setInt(5, track.durationMs());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to insert track: {}", track, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public void addAllTracks(List<Track> tracks) {
        String sql = """
            INSERT INTO track (name, artists, album, isrc, durationMs)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING;
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false); // Optional: beschleunigt bei vielen Inserts
            for (Track track : tracks) {
                stmt.setString(1, track.name());
                var artistsJson = gson.toJson(track.artists());
                stmt.setString(2, artistsJson);
                stmt.setString(3, track.album());
                stmt.setString(4, track.isrc());
                stmt.setInt(5, track.durationMs());
                stmt.addBatch();
            }

            stmt.executeBatch(); // Führt alle in einem Rutsch aus
            conn.commit();       // Bei Bedarf – wenn AutoCommit = false
        } catch (SQLException e) {
            log.error("Failed to add all isrcs", e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public Optional<Track> findByIsrc(String isrc) {
        String sql = "SELECT * FROM track WHERE isrc = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isrc);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTrack(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find track for isrc {}", isrc, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public List<Track> findAll() {
        String sql = "SELECT * FROM track";
        List<Track> tracks = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tracks.add(mapRowToTrack(rs));
            }
            return tracks;
        } catch (SQLException e) {
            log.error("Failed to find all tracks", e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public void update(Track track) {
        String sql = "UPDATE track SET name = ?, artists = ?, album = ?, durationMs = ? WHERE isrc = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, track.name());
            stmt.setString(2, gson.toJson(track.artists()));
            stmt.setString(3, track.album());
            stmt.setInt(4, track.durationMs());
            stmt.setString(5, track.isrc());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update track: {}", track, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public void delete(String isrc) {
        String sql = "DELETE FROM track WHERE isrc = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isrc);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete track for isrc {}", isrc, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public void createTable() {
        String createSql = """
            CREATE TABLE IF NOT EXISTS track (
                name TEXT NOT NULL,
                artists TEXT NOT NULL,
                album TEXT,
                isrc TEXT PRIMARY KEY,
                durationMs INTEGER
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createSql);
        } catch (SQLException e) {
            log.error("Failed to create table track", e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public List<String> findAllIsrcs() {
        String sql = "SELECT isrc FROM track";
        List<String> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(rs.getString("isrc"));
            }
            return result;
        } catch (SQLException e) {
            log.error("Failed to find all isrcs", e);
            throw new DaoException(e.getMessage());
        }
    }


    private Track mapRowToTrack(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        String artistsJson = rs.getString("artists");
        List<String> artists = gson.fromJson(artistsJson, listType);
        String album = rs.getString("album");
        String isrc = rs.getString("isrc");
        int durationMs = rs.getInt("durationMs");
        return new Track(name, artists, album, isrc, durationMs);
    }
}
