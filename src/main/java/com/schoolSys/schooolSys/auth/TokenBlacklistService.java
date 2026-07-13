package com.schoolSys.schooolSys.auth;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory blacklist of access tokens revoked before their natural expiry.
 * <p>
 * Logout revokes the refresh token, but the short-lived access token would
 * otherwise stay valid until it expires. Adding it here makes logout effective
 * immediately. Entries are kept only until the token would have expired, so the
 * map stays bounded. This is a per-instance store, not distributed.
 * </p>
 */
@Service
public class TokenBlacklistService {

    /** Above this many entries, expired tokens are swept on the next write. */
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    /**
     * Revokes a token until the given instant.
     *
     * @param token     the raw access token
     * @param expiresAt when the token would expire on its own
     */
    public void blacklist(String token, Instant expiresAt) {
        if (blacklist.size() > CLEANUP_THRESHOLD) {
            Instant now = Instant.now();
            blacklist.values().removeIf(expiry -> expiry.isBefore(now));
        }
        blacklist.put(token, expiresAt);
    }

    /**
     * @return {@code true} if the token was revoked and has not yet expired
     */
    public boolean isBlacklisted(String token) {
        Instant expiresAt = blacklist.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }
}
