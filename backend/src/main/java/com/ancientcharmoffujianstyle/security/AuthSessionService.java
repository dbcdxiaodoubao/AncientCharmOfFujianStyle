package com.ancientcharmoffujianstyle.security;

import com.ancientcharmoffujianstyle.domain.vo.LoginVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

/** Opaque bearer sessions: persist only SHA-256 hashes; expire and revoke server-side. */
@Service
public class AuthSessionService {
    private final JdbcTemplate jdbc;
    private final long ttlSeconds;
    private final SecureRandom random = new SecureRandom();

    public AuthSessionService(JdbcTemplate jdbc, @Value("${security.session-ttl-seconds:86400}") long ttlSeconds) {
        if (ttlSeconds <= 0) throw new IllegalArgumentException("会话有效期必须大于0");
        this.jdbc = jdbc;
        this.ttlSeconds = ttlSeconds;
    }

    public LoginVo issue(LoginVo user) {
        if (user == null || user.getUserId() == null) throw new IllegalArgumentException("登录用户不存在");
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiry = Instant.now().plusSeconds(ttlSeconds);
        jdbc.update("DELETE FROM auth_session WHERE expires_at <= ?", Timestamp.from(Instant.now()));
        jdbc.update("INSERT INTO auth_session (token_hash, user_id, expires_at) VALUES (?, ?, ?)",
                hash(token), user.getUserId(), Timestamp.from(expiry));
        user.setToken(token);
        user.setExpiresAt(expiry.toEpochMilli());
        return user;
    }

    public Long authenticate(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) return null;
        List<Long> ids = jdbc.query("SELECT s.user_id FROM auth_session s INNER JOIN sys_user u "
                        + "ON u.user_id = s.user_id WHERE s.token_hash = ? AND s.expires_at > ? AND u.status = 0",
                (rs, row) -> rs.getLong("user_id"), hash(token), Timestamp.from(Instant.now()));
        return ids.isEmpty() ? null : ids.get(0);
    }

    public void revoke(String token) {
        jdbc.update("DELETE FROM auth_session WHERE token_hash = ?", hash(token));
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte value : digest) result.append(String.format("%02x", value & 0xff));
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
