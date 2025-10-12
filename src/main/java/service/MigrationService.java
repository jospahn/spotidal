package service;

import dao.IsrcTidalIdDAO;
import dao.TrackDAO;

public class MigrationService {
    private final IsrcTidalIdDAO isrcTidalIdDAO;
    private final TrackDAO trackDAO;

    public MigrationService(IsrcTidalIdDAO isrcTidalIdDAO, TrackDAO trackDAO) {
        this.isrcTidalIdDAO = isrcTidalIdDAO;
        this.trackDAO = trackDAO;
    }
}
