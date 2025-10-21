package model;

public record MusicAccount(
    int id,
    int userId,
    MusicPlatform platform,
    String externalUserId,
    String displayName
) {}
