package model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;

public record Token(
        String accessToken,
        String refreshToken,
        long expiresAt
) {

    public static Token createFromSecondsToLive(String accessToken, String refreshToken, int secondsToLive ) {
        var expiresAt = Instant.now().plusSeconds(secondsToLive).getEpochSecond();
        return new Token(accessToken, refreshToken, expiresAt);
    }

    /**
     * Prüft ob Access Token abgelaufen ist (mit 5min Puffer)
     */
    @JsonIgnore
    public boolean isExpired() {
        return Instant.now().getEpochSecond() + 300 > expiresAt;
    }

    /**
     * Zeit bis zum Ablauf in Sekunden
     */
    @JsonIgnore
    public long getSecondsUntilExpiry() {
        return expiresAt - Instant.now().getEpochSecond();
    }
}
