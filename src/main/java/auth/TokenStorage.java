package auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.MusicService;
import model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Optional;
import java.util.Set;

public class TokenStorage {
    private final static Logger log = LoggerFactory.getLogger(TokenStorage.class);
    private final Path tokenFile;
    private final ObjectMapper objectMapper;

    public TokenStorage(Path tokenFile) {
        this.tokenFile = tokenFile;
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public TokenStorage(MusicService service) {
        var fileName = service.name().toLowerCase() + "-tokens.json";
        this(Path.of(System.getProperty("user.home") + "/.spotidal/" + fileName));
    }
    
    /**
     * Speichert Tokens in einer Datei im User-Home-Verzeichnis
     */
    public void saveTokens(Token token)  {

        Path parentDir = tokenFile.getParent();
        if (!parentDir.toFile().exists()) {
            try {
                Files.createDirectory(parentDir);
            } catch (IOException e) {
                System.out.println("⚠️  Warnung: Konnte Verzeichnis nicht anlege");
                return;
            }
        }
        
        try {
            objectMapper.writeValue(tokenFile.toFile(), token);
        } catch (IOException e) {
            System.out.println("⚠️  Warnung: Konnte token Datei nichtt schreiben");
            return;        }

        try {
            Files.setPosixFilePermissions(
                tokenFile,
                Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
            );
        } catch (UnsupportedOperationException | IOException e) {
            // Windows unterstützt keine POSIX permissions - ignorieren
            System.out.println("⚠️  Warnung: Konnte Datei-Permissions nicht setzen (Windows?)");
        }
        
        System.out.println("💾 Tokens gespeichert in: " + this.tokenFile);
    }

    public Optional<Token> loadTokens()  {
        if (!tokenFile.toFile().exists()) {
            return Optional.empty();
        }

        try {
            var token = objectMapper.readValue(tokenFile.toFile(), Token.class);
            System.out.println("📂 Tokens geladen aus: " + this.tokenFile);
            return Optional.of(token);
        } catch (IOException e) {
            log.error("Error reading tokens", e);
            return Optional.empty();
        }
    }

    public boolean hasStoredTokens() {
        return tokenFile.toFile().exists();
    }

    public void deleteTokens() {
        try {
            Files.deleteIfExists(tokenFile);
        } catch (IOException e) {
            log.error("Error deleting tokens", e);
        }
    }
}
