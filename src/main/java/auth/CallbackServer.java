package auth;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class CallbackServer implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(CallbackServer.class);
    private final URI redirectUri;
    private HttpServer server;

    public CallbackServer(URI redirectUri) {
        this.redirectUri = redirectUri;
    }

    /**
     * Startet den lokalen HTTP-Server zum Abfangen des Callbacks
     */
    public CompletableFuture<String> start() throws IOException {
        var future = new CompletableFuture<String>();

        server = HttpServer.create(new InetSocketAddress(redirectUri.getHost(), redirectUri.getPort()), 0);

        server.createContext("/callback", exchange -> {
            try {
                // Query-Parameter parsen
                String query = exchange.getRequestURI().getQuery();
                String authCode = extractAuthCode(query);

                if (authCode != null) {
                    // Erfolgsseite anzeigen
                    String response = """
                            <html>
                            <head><title>Spotidal Auth</title></head>
                            <body style="font-family: Arial; text-align: center; padding: 50px;">
                                <h1>✓ Authentifizierung erfolgreich!</h1>
                                <p>Du kannst dieses Fenster jetzt schließen.</p>
                            </body>
                            </html>
                            """;

                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }

                    // Code an wartenden Thread übergeben
                    future.complete(authCode);

                } else {
                    // Fehlerseite
                    String errorResponse = """
                            <html>
                            <head><title>Spotidal Auth - Fehler</title></head>
                            <body style="font-family: Arial; text-align: center; padding: 50px;">
                                <h1>❌ Fehler bei der Authentifizierung</h1>
                                <p>Bitte versuche es erneut.</p>
                            </body>
                            </html>
                            """;

                    byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(400, errorBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(errorBytes);
                    }

                    future.completeExceptionally(new Exception("Kein Authorization Code erhalten"));
                }

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        server.start();
        log.info("\uD83C\uDF10 Callback-Server gestartet auf: {}", redirectUri);
        return future;
    }

    /**
     * Stoppt den HTTP-Server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            log.info("🛑 Callback-Server gestoppt");
        }
    }

    private String extractAuthCode(String query) {
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals("code")) {
                return pair[1];
            }
        }
        return null;
    }

    @Override
    public void close() {
        this.stop();
    }
}
