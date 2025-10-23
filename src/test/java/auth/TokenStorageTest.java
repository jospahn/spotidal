package auth;

import spotidal.auth.TokenStorage;
import spotidal.model.Token;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TokenStorageTest {

    @Test
    void itCreatesFolderAndFile(@TempDir Path path) throws IOException {
        var tokensFile = path.resolve(".test/test.tokens");
        var storage = new TokenStorage(tokensFile);
        storage.saveTokens(Token.createFromSecondsToLive("abc", "def", 3600));
        assertTrue(path.resolve(".test/").toFile().exists());
        assertTrue(tokensFile.toFile().exists());
    }

    @Test
    void itPersistsCorrectData(@TempDir Path path) throws IOException{
        var storage = new TokenStorage(path.resolve("test.tokens"));
        var access = RandomStringUtils.insecure().next(30);
        var refresh = RandomStringUtils.insecure().next(30);
        var ttl = 100;

        storage.saveTokens(Token.createFromSecondsToLive(access, refresh, ttl));
        var retrieved = storage.loadTokens().orElseThrow();

        assertEquals(access, retrieved.accessToken());
        assertEquals(refresh, retrieved.refreshToken());
        assertTrue(ttl >= retrieved.getSecondsUntilExpiry());
        assertTrue(ttl-1 <= retrieved.getSecondsUntilExpiry());
    }

}