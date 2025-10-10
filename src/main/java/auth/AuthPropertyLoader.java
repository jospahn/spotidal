package auth;

import model.AuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;

public class AuthPropertyLoader {
    private static final Logger log = LoggerFactory.getLogger(AuthPropertyLoader.class);

    public static Optional<AuthProperties> loadProperties(String serviceName) {
        Properties props = new Properties();
        try (InputStream input = AuthPropertyLoader.class.getClassLoader()
                .getResourceAsStream(serviceName + ".properties")) {

            if (input == null) {
                throw new IOException(serviceName + ".properties nicht gefunden!");
            }

            props.load(input);

        } catch (IOException e) {
            log.error("Fehler beim Laden von {}.properties: ", serviceName, e);
            return Optional.empty();
        }
        return mapProperties(props);
    }

    private static Optional<AuthProperties> mapProperties(Properties props) {
        try {
            URI uri = URI.create(props.getProperty("redirect.uri"));
            return Optional.of(
                    new AuthProperties(props.getProperty("client.id"), props.getProperty("client.secret"), uri)
            );
        } catch (Exception e) {
            log.error("Fehler beim parsen der redirect URI ", e);
            return Optional.empty();
        }
    }
}
