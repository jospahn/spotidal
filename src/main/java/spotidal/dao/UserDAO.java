package spotidal.dao;

import spotidal.model.User;
import java.util.Optional;
import java.util.List;

public interface UserDAO {
    void insert(User user);
    Optional<User> findById(int id);
    List<User> findAll();
    void delete(int id);
    void createTable();
}

