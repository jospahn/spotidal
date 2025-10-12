package database;

import dao.AbstracDatabaseDAO;
import dao.DaoException;
import dao.IsrcTidalIdDAO;
import model.IsrcTidalId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IsrcTidalIdDAOImpl extends AbstracDatabaseDAO implements IsrcTidalIdDAO {
    private static final Logger log = LoggerFactory.getLogger(IsrcTidalIdDAOImpl.class);

    public IsrcTidalIdDAOImpl(Connection conn) {
        super(conn);
    }

    @Override
    public void insert(IsrcTidalId isrcTidalId) {
        String sql = "INSERT INTO isrc_tidal_ids (isrc, tidal_id, failed) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isrcTidalId.isrc());
            stmt.setString(2, isrcTidalId.tidalId());
            stmt.setBoolean(3, isrcTidalId.failed());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to insert isrc_tidal_ids: {}", isrcTidalId, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public Optional<IsrcTidalId> findByIsrc(String isrc) {
        String sql = "SELECT * FROM track WHERE isrc = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isrc);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new IsrcTidalId(
                            rs.getString("isrc"),
                            rs.getString("tidal_id"),
                            rs.getBoolean("failed")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find isrc_tidal_ids for isrc {}", isrc, e);
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
            log.error("Failed to delete isrc_tidal_ids for isrc {}", isrc, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public void createTable() {
        String createSql = """
            CREATE TABLE IF NOT EXISTS isrc_tidal_ids (
                isrc TEXT PRIMARY KEY NOT NULL,
                tidal_id TEXT,
                failed BOOLEAN NOT NULL DEFAULT FALSE
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createSql);
        } catch (SQLException e) {
            log.error("Failed to create table isrc_tidal_ids", e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public void update(IsrcTidalId isrcTidalId) {
        String sql = "UPDATE isrc_tidal_ids SET tidal_id = ?, failed = ? WHERE isrc = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isrcTidalId.tidalId());
            stmt.setBoolean(2, isrcTidalId.failed());
            stmt.setString(3, isrcTidalId.isrc());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update isrc_tidal_ids: {}", isrcTidalId, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public List<IsrcTidalId> findUpdateNeeded(int count)  {
        String sql = """
                SELECT isrc
                FROM isrc_tidal_ids
                WHERE failed = false AND tidal_id IS NULL
                LIMIT ?;
                """;
        List<IsrcTidalId> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
                stmt.setInt(1, count);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    result.add(IsrcTidalId.create(rs.getString("isrc")));
                }
                return result;
        } catch (SQLException e) {
            log.error("Failed to find update needed", e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public void addAllIsrc(List<String> isrcs) {
        String sql = """
            INSERT INTO isrc_tidal_ids (isrc, tidal_id, failed)
            VALUES (?, NULL, FALSE)
            ON CONFLICT DO NOTHING;
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false); // Optional: beschleunigt bei vielen Inserts
            for (String isrc : isrcs) {
                stmt.setString(1, isrc);
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
    public int count() {
        String sql = "SELECT COUNT(*) FROM isrc_tidal_ids";
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            log.error("Failed to count isrc_tidal_ids", e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public int countFailed() {
        String sql = """
                SELECT COUNT(*)
                FROM isrc_tidal_ids
                WHERE failed = true;
                """;
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            log.error("Failed to count failed entries", e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public int countSuccess() {
        String sql = """
                SELECT COUNT(*)
                FROM isrc_tidal_ids
                WHERE tidal_id IS NOT NULL AND failed = false;
                """;
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            log.error("Failed to count successfull entries", e);
            throw new DaoException(e.getMessage());
        }
    }
}
