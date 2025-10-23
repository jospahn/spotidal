package spotidal.service;

import spotidal.ServiceRegistry;
import spotidal.auth.SpotifyAuthService;
import spotidal.auth.TidalAuthService;
import spotidal.dao.UserDAO;

public class UserService {
    private final SpotifyService spotifyService;
    private final TidalService tidalService;
    private final UserDAO userDAO;
//    private final SpotifyAuthService spotifyAuthService;
    private final TidalAuthService tidalAuthService;

    public UserService(ServiceRegistry context) {
        this.spotifyService = context.getSpotifyService();
        this.tidalService = context.getTidalService();
        this.userDAO = context.getDaoProvider().getUserDAO();
//        this.spotifyAuthService = new SpotifyAuthService();
        this.tidalAuthService = new TidalAuthService();
    }

    public void fetchAndStoreSpotifyUser() {
        // Authentifizierung sicherstellen
//        spotifyAuthService.authenticate();
        try {
            String userId = spotifyService.getCurrentUserId();
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
