import auth.SpotifyAuthService;
import auth.TidalAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import service.TidalService;
import service.SpotifyService;

import java.util.concurrent.Callable;

@Command(
        name = "spotify-to-tidal",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Migriert Playlists und Liked Songs von Spotify zu Tidal"
)
public class Spotidal implements Callable<Integer> {
    private static final Logger log = LoggerFactory.getLogger(Spotidal.class);


    @Command(name = "test")
    int test() {
        log.info("test called");


        var tidal = new TidalService();
        var id = tidal.getTrackIdByIsrc("GBN9Y1100088");

        id.ifPresent(System.out::println);

        var d = tidal.getUserCollection();
        d.forEach(System.out::println);

        return 0;
    }


    @Command(name = "export", description = "Exportiert Spotify Daten")
    int export(
            @Option(names = {"-t", "--type"},
                    description = "Was exportieren: playlists, liked-songs, all",
                    defaultValue = "all") String type,
            @Option(names = {"-o", "--output"},
                    description = "Output Datei",
                    defaultValue = "spotify-export.json") String output
    ) {
        log.info("Exportiere {} von Spotify...", type);
        SpotifyService spotify = new SpotifyService();

        try {
            switch (type.toLowerCase()) {
                case "playlists":
                    spotify.exportPlaylists(output);
                    break;
                case "liked-songs":
                    spotify.exportLikedSongs(output);
                    break;
                case "all":
                    spotify.exportAll(output);
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

    @Command(name = "import", description = "Importiert Daten zu Tidal")
    int importToTidal(
            @Option(names = {"-i", "--input"},
                    description = "Input JSON Datei",
                    required = true) String input
    ) {
        log.info("Importiere zu Tidal von " + input + "...");

        TidalService tidal = new TidalService();
      //  MigrationService migration = new MigrationService(tidal);

        try {
        //    migration.importFromFile(input);
            log.info("✓ Import erfolgreich");
            return 0;

        } catch (Exception e) {
            log.error("Fehler beim Import", e);
            return 1;
        }
    }

    @Command(name = "migrate", description = "Direkte Migration von Spotify zu Tidal")
    int migrate(
            @Option(names = {"-t", "--type"},
                    description = "Was migrieren: playlists, liked-songs, all",
                    defaultValue = "all") String type
    ) {
        log.info("Starte Migration von Spotify zu Tidal...");

        SpotifyService spotify = new SpotifyService();
        TidalService tidal = new TidalService();
      //  MigrationService migration = new MigrationService(spotify, tidal);

        try {
        //    migration.migrate(type);
            log.info("✓ Migration erfolgreich");
            return 0;

        } catch (Exception e) {
            log.error("Fehler bei der Migration", e);
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

    static void main(String[] args) {
        int exitCode = new CommandLine(new Spotidal()).execute(args);
        System.exit(exitCode);
    }
}