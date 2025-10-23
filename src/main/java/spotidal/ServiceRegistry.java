package spotidal;

import spotidal.auth.AuthService;
import spotidal.auth.SpotifyAuthService;
import spotidal.auth.TidalAuthService;
import spotidal.database.DaoProvider;
import spotidal.service.LocalDataService;
import spotidal.service.SpotifyService;
import spotidal.service.TidalApiService;
import spotidal.service.TidalService;

import java.sql.SQLException;

public class ServiceRegistry {
    private final AuthService spotifyAuthService;
    private final AuthService tidalAuthService;
    private final DaoProvider daoProvider;
    private final TidalApiService tidalApiService;
    private final TidalService tidalService;
    private final SpotifyService spotifyService;
    private final LocalDataService localDataService;

    public ServiceRegistry() throws SQLException {
        daoProvider = new DaoProvider("spotidal.db");

        spotifyAuthService = new SpotifyAuthService();
        tidalAuthService = new TidalAuthService();

        tidalApiService = new TidalApiService(tidalAuthService);
        tidalService = new TidalService(tidalApiService, daoProvider.getIsrcTidalIdDAO());
        spotifyService = new SpotifyService(spotifyAuthService);
        localDataService = new LocalDataService(daoProvider.getTrackDAO());
    }

    public DaoProvider getDaoProvider() { return daoProvider; }

    public AuthService getSpotifyAuthService() {
        return spotifyAuthService;
    }
    public AuthService getTidalAuthService() {
        return tidalAuthService;
    }

    public TidalApiService getTidalApiService() { return tidalApiService; }
    public TidalService getTidalService() { return tidalService; }
    public SpotifyService getSpotifyService() { return spotifyService; }
    public LocalDataService getLocalDataService() { return localDataService; }


}
