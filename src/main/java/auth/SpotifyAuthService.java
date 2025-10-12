package auth;

import model.MusicPlatform;
import model.Token;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

public class SpotifyAuthService {
    public static final Logger log = LoggerFactory.getLogger(SpotifyAuthService.class.getName());
    private final URI redirectUri;
    private final SpotifyApi spotifyApi;
    private final TokenStorage storage = new TokenStorage(MusicPlatform.SPOTIFY);

    public SpotifyAuthService() {
        var authProperties = AuthPropertyLoader.loadProperties("spotify").orElseThrow();

        this.redirectUri = authProperties.redirectUri();

        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(authProperties.clientId())
                .setClientSecret(authProperties.clientSecret())
                .setRedirectUri(authProperties.redirectUri())
                .build();
    }

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

    public Optional<Token> refresh(Token token) {
        spotifyApi.setRefreshToken(token.refreshToken());
        var request = spotifyApi.authorizationCodeRefresh().build();
        try {
            var credentials = request.execute();
            var newToken = Token.createFromSecondsToLive(
                    credentials.getAccessToken(),
                    credentials.getRefreshToken(),
                    credentials.getExpiresIn()
            );
            storage.deleteTokens();
            storage.saveTokens(newToken);
            log.info("✓ Token refresh erfolgreich!");
            log.info("  Access Token expires in: " + credentials.getExpiresIn() + " seconds");

            return Optional.of(newToken);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Startet den kompletten Auth-Flow und gibt ein fertig konfiguriertes SpotifyApi-Objekt zurück
     */
    public Optional<Token> authenticate()  {

        try (var server = new CallbackServer(redirectUri)){

            var authCodeFuture = server.start();
            openAuthUrlInBrowser();

            // 3. Warten auf Callback (blockierend)
            log.info("⏳ Warte auf Authentifizierung im Browser...");
            String authCode = authCodeFuture.get(); // Blockiert bis Code empfangen

            // 5. Access Token holen
            AuthorizationCodeCredentials credentials = exchangeCodeForToken(authCode);

            // 6. SpotifyApi mit Token konfigurieren
            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken());

            var token = Token.createFromSecondsToLive(credentials.getAccessToken(), credentials.getRefreshToken(), credentials.getExpiresIn());

            log.info("✓ Authentifizierung erfolgreich!");
            log.info("  Access Token expires in: " + credentials.getExpiresIn() + " seconds");

            storage.saveTokens(token);
            return Optional.of(token);
        } catch (Exception e) {
            log.error("Failed to authenticate", e);
            return Optional.empty();
        }
    }

    /**
     * Öffnet die Spotify Auth-URL im Standard-Browser
     */
    private void openAuthUrlInBrowser() throws Exception {
        AuthorizationCodeUriRequest authRequest = spotifyApi.authorizationCodeUri()
                .scope("user-library-read,playlist-read-private,playlist-read-collaborative")
                .show_dialog(true)
                .build();
        
        URI authUri = authRequest.execute();
        
        log.info("🔐 Öffne Browser für Authentifizierung...");
        log.info("    Auth-URL: " + authUri);
        log.info("    Redirect URI: " + redirectUri);
        log.info("    Falls der Browser nicht automatisch öffnet, besuche die Auth-URL oben.");
        
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(authUri);
        }
    }
    
    /**
     * Tauscht den Authorization Code gegen Access Token
     */
    private AuthorizationCodeCredentials exchangeCodeForToken(String code) throws Exception {
        AuthorizationCodeRequest tokenRequest = spotifyApi.authorizationCode(code).build();
        return tokenRequest.execute();
    }
}
