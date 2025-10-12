import auth.SpotifyAuthService;
import auth.TidalAuthService;
import dao.IsrcTidalIdDAO;
import database.DaoProvider;
import model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import service.LocalDataService;
import service.TidalApiService;
import service.SpotifyService;
import service.TidalService;

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
    private final DaoProvider daoProvider;

    Spotidal() throws SQLException {
        this.daoProvider = new DaoProvider("spotidal.db");
    }

    @Command(name = "test")
    int test()  {
        log.info("test called");

        IsrcTidalIdDAO dao = daoProvider.getIsrcTidalIdDAO();
        var count = dao.count();
        log.info("Anzahl der Isrcs: {}", count);
        var list = dao.findUpdateNeeded(20);

        for (var isrcTidalId : list) {
            log.info("{}", isrcTidalId);
        }

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
    )  {
        var tidal = new TidalService(new TidalApiService(), daoProvider.getIsrcTidalIdDAO());

        if (addTrackIsrc) {
            log.info("Loading tracks to the isrc<->tidalId table");
            var tracks = daoProvider.getTrackDAO().findAll();
            tidal.addIsrcToIsrcTidalIds(tracks.stream().map(Track::isrc).toList());
        }

        if (updateIsrc) {
            log.info("Updating entries in the isrc<->tidalId table with tidal track ids");
            tidal.updateIsrcTidalIds();
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
        log.info("Exportiere {} von Spotify...", type);
        SpotifyService spotify = new SpotifyService();

        try {
            switch (type.toLowerCase()) {
                case "playlists":
                    break;
                case "liked-songs":
                    var tracks = spotify.loadAllLikedSongs();
                    var dataService = new LocalDataService(daoProvider.getTrackDAO());
                    dataService.insertAllTracks(tracks);
                    break;
                case "all":
                    break;
                default:
                    log.error("Unbekannter Type: {}", type);
                    return 1;
            }

            log.info("✓ Export erfolgreich nach {}", output);
            return 0;

        } catch (Exception e) {
           log.error("Fehler beim Export", e);
           return 1;
        }
    }


    @Command(name = "auth", description = "Authentifizierung einrichten")
    int auth(
            @Option(names = {"-s", "--service"},
                    description = "Service: spotify, tidal, both",
                    defaultValue = "both") String service
    ) {
        log.info("Richte Authentifizierung ein für: {}", service);

        var sas = new SpotifyAuthService();
        var tas = new TidalAuthService();

        switch (service.toLowerCase()) {
            case "spotify":
                sas.authenticate();
                break;
            case "tidal":
                tas.authenticate();
                break;
            case "both":
                sas.authenticate();
                tas.authenticate();
                break;
        }

        return 0;
    }

    @Override
    public Integer call() {
        // Wenn keine Subcommand angegeben, zeige Help
        CommandLine.usage(this, System.out);
        return 0;
    }

    static void main(String[] args) throws SQLException {
        int exitCode = new CommandLine(new Spotidal()).execute(args);
        System.exit(exitCode);
    }
}