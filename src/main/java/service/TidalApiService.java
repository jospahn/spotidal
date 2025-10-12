package service;

import auth.TidalAuthService;
import com.google.gson.reflect.TypeToken;
import http.ApiException;
import http.GsonBodyHandler;
import http.RequestLimiter;
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

public class TidalApiService {
    private static final Logger log = LoggerFactory.getLogger(TidalApiService.class);
    private final TidalAuthService authService = new TidalAuthService();
    private String userId = "";
    private Token token;
    private final RequestLimiter requestLimiter;

    public TidalApiService() {
        this.requestLimiter = new RequestLimiter(2);
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
                .map(list -> list.getFirst().id());
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
        requestLimiter.waitForNextRequest();
        try(HttpClient client = HttpClient.newBuilder().build()) {
            var response = client.send(request, handler);
            if (response.statusCode() == 429) {
                var retryAfter = response.headers().firstValue("Retry-After").orElse("0");
                log.error("Rate limit exceeded! Retry after {} seconds", retryAfter);
                requestLimiter.updateNextRequestAllowed(Integer.parseInt(retryAfter));
                throw new ApiException(response.statusCode(), retryAfter);
            }
            if (response.statusCode() != 200) {
                log.error("Failed to send request, status code: {}", response.statusCode());
                log.error(response.toString());
                log.error("Response body: {}", response.body());
                throw new ApiException(response.statusCode());
            }
            return Optional.of(response.body());
        } catch (IOException | InterruptedException e) {
            log.error("Failed send request", e);
        }
        return Optional.empty();
    }
}
