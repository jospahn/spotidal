package spotidal.database;

import spotidal.dao.UserDAO;
import spotidal.dao.DaoException;
import spotidal.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {
    private static final Logger log = LoggerFactory.getLogger(UserDAOImpl.class);
    private final Connection conn;

    public UserDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(User user) {
        String sql = "INSERT INTO User DEFAULT VALUES";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to insert user: {}", user, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM User WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find user for id {}", id, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM User";
        List<User> users = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            log.error("Failed to find all users", e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM User WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete user for id {}", id, e);
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public void createTable() {
        String createSql = """
            CREATE TABLE IF NOT EXISTS User (
                id INTEGER PRIMARY KEY
            );
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createSql);
        } catch (SQLException e) {
            log.error("Failed to create table User", e);
            throw new DaoException(e.getMessage());
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        return new User(id);
    }
}