package model;

public record IsrcTidalId(
        String isrc,
        String tidalId,
        boolean failed
) {
    public static IsrcTidalId create(String isrc) {
        return new IsrcTidalId(isrc, null, false);
    }

    public IsrcTidalId setTidalId(String tidalId) {
        return new IsrcTidalId(isrc, tidalId, false);
    }

    public IsrcTidalId setFailed() {
        return new IsrcTidalId(isrc, null, true);
    }

    public boolean needsUpdate() {
        return !hasTidalId() || !failed;
    }

    public boolean hasTidalId() {
        return tidalId != null;
    }
}
