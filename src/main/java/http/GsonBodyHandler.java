package http;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import com.google.gson.Gson;
import java.io.InputStreamReader;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class GsonBodyHandler<T> implements HttpResponse.BodyHandler<T> {
    private final Type type;
    private final Gson gson = new Gson();

    public GsonBodyHandler(Type type) {
        this.type = type;
    }

    @Override
    public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
        return HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofInputStream(),
                this::fromInputStream
        );
    }

    private T fromInputStream(InputStream inputStream) {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

    public static <T> GsonBodyHandler<T> of(TypeToken<T> typeToken) {
        return new GsonBodyHandler<>(typeToken.getType());
    }
}

