package uz.common.security;

public final class JwtBlacklist {

    private JwtBlacklist() {
    }

    private static final String KEY_PREFIX = "jwt:blacklist:";

    public static String key(String jti) {
        return KEY_PREFIX + jti;
    }
}