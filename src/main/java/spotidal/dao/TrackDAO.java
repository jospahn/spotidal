package spotidal.dao;

import spotidal.model.Track;

import java.util.List;
import java.util.Optional;

public interface TrackDAO {
    void insert(Track track);
    void addAllTracks(List<Track> tracks);
    Optional<Track> findByIsrc(String isrc);
    List<Track> findAll();
    void update(Track track);
    void delete(String isrc);
    void createTable();
    List<String> findAllIsrcs();
}
