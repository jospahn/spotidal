package service;

import auth.TidalAuthService;
import com.google.gson.reflect.TypeToken;
import http.GsonBodyHandler;
import model.Token;
import model.tidal.DataListResponse;
import model.tidal.DataResponse;
import model.tidal.Me;
import model.tidal.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Optional;

public class TidalService {
    private static final Logger log = LoggerFactory.getLogger(TidalService.class);
    private final TidalAuthService authService = new TidalAuthService();
    private String userId = "";
    private Token token;

    public TidalService() {

    }

    private String resolveAccessToken() {
        if (token != null && !token.isExpired()) {
            return token.accessToken();
        }
        token = authService.checkAndRefresh().orElseThrow();
        return token.accessToken();
    }


    public String resolveUserId() {
        if (!userId.isEmpty()) {
            return userId;
        }

        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://openapi.tidal.com/v2/users/me"))
                .header("Authorization", "Bearer " + resolveAccessToken())
                .build();

        var response = sendRequest(request, new TypeToken<DataResponse<Me>>() {});
        response.ifPresent(r -> userId = r.data().id());
        return userId;
    }

    public Optional<String> getTrackIdByIsrc(String isrc) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://openapi.tidal.com/v2/tracks?filter%5Bisrc%5D=" + isrc))
                .header("Authorization", "Bearer " + resolveAccessToken())
                .build();

        var response = sendRequest(request, new TypeToken<DataListResponse<Resource>>() {});
        return response
                .map(DataListResponse::data)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0).id());
    }

    public List<Resource> getUserCollection() {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://openapi.tidal.com/v2/userCollections/" + resolveUserId() + "/relationships/tracks"))
                .header("Authorization", "Bearer " + resolveAccessToken())
                .build();

        var response = sendRequest(request, new TypeToken<DataListResponse<Resource>>() {});

        return response.map(DataListResponse::data).orElse(List.of());
    }
    
    private <T> Optional<T> sendRequest(HttpRequest request, TypeToken<T> typeToken) {
        GsonBodyHandler<T> handler = GsonBodyHandler.of(typeToken);
        try(HttpClient client = HttpClient.newBuilder().build()) {
            var response = client.send(request, handler);
            return Optional.of(response.body());
        } catch (IOException | InterruptedException e) {
            log.error("Failed send request", e);
        }
        return Optional.empty();
    }
}
