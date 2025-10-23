package spotidal.auth;

import spotidal.model.AuthProperties;
import spotidal.model.MusicPlatform;
import spotidal.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

public abstract class AuthService {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final AuthProperties authProperties;
    protected final TokenStorage storage;

    public AuthService(MusicPlatform musicPlatform) {
        this.authProperties = AuthPropertyLoader.loadProperties(musicPlatform.name().toLowerCase()).orElseThrow();
        this.storage = new TokenStorage(musicPlatform);
    }

    public abstract Optional<Token> authenticate();
    public abstract Optional<Token> refresh(Token oldToken);

    public Optional<Token> checkAndRefresh() {
        if (!storage.hasStoredTokens()) {
            return authenticate();
        }
        var token = storage.loadTokens();
        if (token.isEmpty()) {
            return authenticate();
        }
        if (token.get().isExpired()) {
            return refresh(token.get());
        }
        log.info("Gültiges token vorhanden");
        return token;
    }
}

