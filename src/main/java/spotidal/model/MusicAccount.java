package spotidal.model;

public record MusicAccount(
    Integer id,
    Integer userId,
    MusicPlatform platform,
    String externalUserId,
    String displayName
) {}
