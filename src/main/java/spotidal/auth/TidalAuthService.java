package spotidal.auth;

import com.google.gson.GsonBuilder;
import spotidal.model.AuthProperties;
import spotidal.model.MusicPlatform;
import spotidal.model.tidal.TidalTokenResponse;
import spotidal.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class TidalAuthService extends AuthService {
    public TidalAuthService() {
        super(MusicPlatform.TIDAL);
    }

    @Override
    public Optional<Token> refresh(Token oldToken) {
        log.info("Refreshing token");
        var refreshToken = oldToken.refreshToken();
        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://auth.tidal.com/v1/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("refresh_token=" + refreshToken + "&grant_type=refresh_token"))
                .build();

        var tidalTokenResponse = sendTokenRequest(request);

        if (tidalTokenResponse.isEmpty()) {
            return Optional.empty();
        }
        var tt = tidalTokenResponse.get();
        return Optional.of(Token.createFromSecondsToLive(
                tt.accessToken(),
                oldToken.refreshToken(),
                tt.expiresIn()
        ));

    }

    @Override
    public Optional<Token> authenticate() {
        var codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier).orElseThrow();
        URI authUri = buildAuthorizationUrl(codeChallenge);
        var authCode = getAuthCode(authUri).orElseThrow();
        log.info("Auth Code: {}", authCode);
        var token = exchangeCodeForToken(authCode, codeVerifier);
        token.ifPresent(storage::saveTokens);

        return token;
    }

    private Optional<TidalTokenResponse> sendTokenRequest(HttpRequest request) {
        try(HttpClient client = HttpClient.newBuilder().build()) {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            var gson = new GsonBuilder().create();
            var tokens = gson.fromJson(response.body(), TidalTokenResponse.class);
            log.info("Token erfolgreich abgefragt, expires in {} seconds", tokens.expiresIn());
            return Optional.of(tokens);
        } catch (IOException | InterruptedException e) {
            log.error("Failed to exchange code for token", e);
        }
        return Optional.empty();
    }


    private Optional<String> getAuthCode(URI uri) {
        try(var callbackServer = new CallbackServer(authProperties.redirectUri())) {
            var authCodeFuture = callbackServer.start();
            Desktop.getDesktop().browse(uri);
            var code = authCodeFuture.get();
            return Optional.of(code);
        } catch (IOException e) {
            log.error("Failed to open browser or to start callback server!", e);
        } catch (ExecutionException e) {
            log.error("Exception waiting for callback", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Exception waiting for callback", e);
        }


        return Optional.empty();
    }

    private Optional<Token> exchangeCodeForToken(String authCode, String codeVerifier) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://auth.tidal.com/v1/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("code=" + authCode + "&code_verifier=" + codeVerifier + "&grant_type=authorization_code&client_id=" + authProperties.clientId() + "&client_secret=" + authProperties.clientSecret() + "&redirect_uri=" + URLEncoder.encode(authProperties.redirectUri().toString(), StandardCharsets.UTF_8)))
                .build();

        var tidalTokenResponse = sendTokenRequest(request);
        if (tidalTokenResponse.isPresent()) {
            var tt = tidalTokenResponse.get();
            return Optional.of(Token.createFromSecondsToLive(
                    tt.accessToken(),
                    tt.refreshToken(),
                    tt.expiresIn()
            ));
        }
        return Optional.empty();
    }

    private URI buildAuthorizationUrl(String codeChallenge) {
        return URI.create("https://login.tidal.com/authorize" +
                "?response_type=code" +
                "&client_id=" + authProperties.clientId() +
                "&redirect_uri=" + URLEncoder.encode(authProperties.redirectUri().toString(), StandardCharsets.UTF_8) +
                "&scope=" + AuthUtils.encodeScopes("collection.read", "collection.write", "playlists.read", "playlists.write", "user.read") +
                "&code_challenge=" + codeChallenge +
                "&code_challenge_method=S256");
    }

    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] code = new byte[32];
        secureRandom.nextBytes(code);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code);
    }

    private Optional<String> generateCodeChallenge(String codeVerifier)  {
        byte[] bytes = codeVerifier.getBytes(StandardCharsets.UTF_8);
        try {
            var messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(bytes);
            return Optional.of(Base64.getUrlEncoder().withoutPadding().encodeToString(digest));
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate code challenge", e);
            return Optional.empty();
        }
    }
}
