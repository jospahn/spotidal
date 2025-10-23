package spotidal.model;

import java.net.URI;

public record AuthProperties(
        String clientId,
        String clientSecret,
        URI redirectUri
) {

}
