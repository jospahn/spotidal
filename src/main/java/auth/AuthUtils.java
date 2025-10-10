package auth;
public class AuthUtils {
    private AuthUtils() { }

    public static String encodeScopes(String... scopes) {
        return String.join("%20", scopes);
    }
}
