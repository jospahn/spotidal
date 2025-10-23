package spotidal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import spotidal.model.Track;

import java.sql.SQLException;
import java.util.concurrent.Callable;

@Command(
        name = "spotify-to-tidal",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Migriert Playlists und Liked Songs von Spotify zu Tidal"
)
public class Spotidal implements Callable<Integer> {
    private static final Logger log = LoggerFactory.getLogger(Spotidal.class);
    private final ServiceRegistry serviceRegistry;
    public Spotidal(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Command(name = "test")
    int test() {
        serviceRegistry.getTidalApiService().resolveUserId();
        return 0;
    }

    @Command(name = "migrate", description = "Migriert Playlists und Liked Songs von Spotify zu Tidal")
    int migrate(
            @Option(names = {"-t", "--add-track-isrc"},
                    description = "Übertrage tracks in die lokale isrc liste",
                    defaultValue = "false") boolean addTrackIsrc,
            @Option(names = {"-u", "--update-isrc"},
                    description = "Lade tidal track ids in die lokale isrc liste",
                    defaultValue = "false") boolean updateIsrc
    ) {
        var daoProvider = serviceRegistry.getDaoProvider();
        var tidalService = serviceRegistry.getTidalService();
        if (addTrackIsrc) {
            var tracks = daoProvider.getTrackDAO().findAll();
            tidalService.addIsrcToIsrcTidalIds(tracks.stream().map(Track::isrc).toList());
        }
        if (updateIsrc) {
            tidalService.updateIsrcTidalIds();
        }
        var count = daoProvider.getIsrcTidalIdDAO().count();
        var failed = daoProvider.getIsrcTidalIdDAO().countFailed();
        var success = daoProvider.getIsrcTidalIdDAO().countSuccess();
        log.info("Now {} tracks in isrc<->tidalId table, {} with tidalId and {} failed", count, success, failed);
        return 0;
    }

    @Command(name = "export", description = "Exportiert Spotify Daten")
    int export(
            @Option(names = {"-t", "--type"},
                    description = "Was exportieren: playlists, liked-songs, all",
                    defaultValue = "liked-songs") String type,
            @Option(names = {"-o", "--output"},
                    description = "Output Datei",
                    defaultValue = "spotify-export.json") String output
    ) {
//        log.info("Exportiere {} von Spotify...", type);
//        var spotifyService = applicationContext.getSpotifyService();
//        var localDataService = applicationContext.getLocalDataService();
//        try {
//            switch (type.toLowerCase()) {
//                case "liked-songs":
//                    var tracks = spotifyService.loadAllLikedSongs();
//                    localDataService.insertAllTracks(tracks);
//                    break;
//                case "playlists":
//                    // TODO: Implementiere Playlist-Export
//                    break;
//                case "all":
//                    // TODO: Implementiere Export aller Daten
//                    break;
//                default:
//                    log.error("Unbekannter Type: {}", type);
//                    return 1;
//            }
//            log.info("✓ Export erfolgreich nach {}", output);
//            return 0;
//        } catch (Exception e) {
//            log.error("Fehler beim Export", e);
            return 1;
//        }
    }

    @Command(name = "spotidal/auth", description = "Authentifizierung einrichten")
    int auth(
            @Option(names = {"-s", "--service"},
                    description = "Service: spotify, tidal, both",
                    defaultValue = "both") String service
    ) {
//        log.info("Richte Authentifizierung ein für: {}", service);
//        switch (service.toLowerCase()) {
//            case "spotify" -> new spotidal.auth.SpotifyAuthService().authenticate();
//            case "tidal" -> new auth.TidalAuthService().authenticate();
//            case "both" -> {
//                new spotidal.auth.SpotifyAuthService().authenticate();
//                new auth.TidalAuthService().authenticate();
//            }
//            default -> {
//                log.error("Unbekannter Service: {}", service);
//                return 1;
//            }
//        }
        return 0;
    }

    @Override
    public Integer call() {
        // Wenn keine Subcommand angegeben, zeige Help
        CommandLine.usage(this, System.out);
        return 0;
    }

    static void main(String[] args) throws SQLException {
        try {
            var serviceRegistry = new ServiceRegistry();
            var tui = new SpotidalTUI(serviceRegistry);
            tui.start();
        } catch (Exception e) {
            log.error("Fehler beim Starten des Terminal-UI", e);
            System.exit(1);
        }
    }
}