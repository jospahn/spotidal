package spotidal.service;

import spotidal.ServiceRegistry;
import spotidal.auth.AuthService;
import spotidal.auth.SpotifyAuthService;
import spotidal.auth.TidalAuthService;
import spotidal.dao.UserDAO;
import spotidal.database.DaoProvider;
import spotidal.model.MusicAccount;
import spotidal.model.MusicPlatform;

public class UserService {
    private final SpotifyService spotifyService;
    private final TidalService tidalService;
    private final AuthService spotifyAuthService;
    private final AuthService tidalAuthService;
    private final DaoProvider daoProvider;

    public UserService(ServiceRegistry registry) {
        this.daoProvider = registry.getDaoProvider();
        this.spotifyService = registry.getSpotifyService();
        this.tidalService = registry.getTidalService();
        this.spotifyAuthService = registry.getSpotifyAuthService();
        this.tidalAuthService = registry.getTidalAuthService();
    }

    public void authenticateSpotify() {
        spotifyAuthService.authenticate();
    }

    public void fetchAndStoreSpotifyUser() {

        try {
            String userId = spotifyService.getCurrentUserId();
            MusicAccount account = new MusicAccount(0, 0, MusicPlatform.SPOTIFY, userId, "");
            daoProvider.getMusicAccountDAO().insert(account);
//            User user = new User(userId, "spotify");
//            userDAO.save(user);
        } catch (Exception e) {
            // Fehlerbehandlung
        }
    }

    public void fetchAndStoreTidalUser() {
        tidalAuthService.authenticate();
        try {
            String userId = tidalService.resolveUser();
//            User user = new User(userId, "tidal");
//            userDAO.save(user);
        } catch (Exception e) {
            // Fehlerbehandlung
        }
    }
}
