package spotidal.dao;

import spotidal.model.MusicAccount;
import java.util.List;
import java.util.Optional;

public interface MusicAccountDAO {
    void insert(MusicAccount account);
    Optional<MusicAccount> findById(int id);
    List<MusicAccount> findByUserId(int userId);
    Optional<MusicAccount> findByPlatformAndExternalUserId(String platform, String externalUserId);
    List<MusicAccount> findAll();
    void delete(int id);
    void createTable();
}


