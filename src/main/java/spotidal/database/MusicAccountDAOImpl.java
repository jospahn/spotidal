package spotidal.database;

import spotidal.dao.MusicAccountDAO;
import spotidal.dao.DaoException;
import spotidal.model.MusicAccount;
import spotidal.model.MusicPlatform;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MusicAccountDAOImpl extends AbstractDatabaseDAO implements MusicAccountDAO {

    public MusicAccountDAOImpl(Connection conn) {
        super(conn);
    }

    @Override
    public void insert(MusicAccount account) {
        String sql = "INSERT INTO MusicAccount (user_id, platform, external_user_id, display_name) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (account.userId() == null) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, account.userId());
            }
            stmt.setString(2, account.platform().name()); // Enum als String speichern
            stmt.setString(3, account.externalUserId());
            stmt.setString(4, account.displayName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to insert music account: {}", account, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public Optional<MusicAccount> findById(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM MusicAccount WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToMusicAccount(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find music account for id {}", id, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public List<MusicAccount> findByUserId(Integer userId) {
        List<MusicAccount> accounts = new ArrayList<>();
        if (userId == null) return accounts;
        String sql = "SELECT * FROM MusicAccount WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapRowToMusicAccount(rs));
                }
            }
            return accounts;
        } catch (SQLException e) {
            log.error("Failed to find music accounts for user_id {}", userId, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public Optional<MusicAccount> findByPlatformAndExternalUserId(String platform, String externalUserId) {
        String sql = "SELECT * FROM MusicAccount WHERE platform = ? AND external_user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, platform);
            stmt.setString(2, externalUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToMusicAccount(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find music account for platform {} and external_user_id {}", platform, externalUserId, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public List<MusicAccount> findAll() {
        String sql = "SELECT * FROM MusicAccount";
        List<MusicAccount> accounts = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapRowToMusicAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            log.error("Failed to find all music accounts", e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM MusicAccount WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete music account for id {}", id, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public void createTable() {
        String createSql = """
            CREATE TABLE IF NOT EXISTS MusicAccount (
                id INTEGER PRIMARY KEY,
                user_id INTEGER,
                platform TEXT NOT NULL,
                external_user_id TEXT NOT NULL,
                display_name TEXT,
//                FOREIGN KEY (user_id) REFERENCES User(id),
                UNIQUE(platform, external_user_id)
            );
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createSql);
        } catch (SQLException e) {
            log.error("Failed to create table MusicAccount", e);
            throw new DaoException(e.getMessage());
        }
    }

    private MusicAccount mapRowToMusicAccount(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Integer userId = rs.getObject("user_id") == null ? null : rs.getInt("user_id");
        String platformStr = rs.getString("platform");
        MusicPlatform platform = MusicPlatform.valueOf(platformStr); // String zu Enum
        String externalUserId = rs.getString("external_user_id");
        String displayName = rs.getString("display_name");
        return new MusicAccount(id, userId, platform, externalUserId, displayName);
    }
}
