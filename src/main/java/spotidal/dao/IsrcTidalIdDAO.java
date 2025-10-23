package spotidal.dao;

import spotidal.model.IsrcTidalId;

import java.util.List;
import java.util.Optional;

public interface IsrcTidalIdDAO {
    void insert(IsrcTidalId isrcTidalId);
    Optional<IsrcTidalId> findByIsrc(String isrc);
    void delete(String isrc);
    void createTable();
    void update(IsrcTidalId isrcTidalId);
    List<IsrcTidalId> findUpdateNeeded(int count);
    void addAllIsrc(List<String> isrcTidalIds);
    int count();
    int countFailed();
    int countSuccess();
}
