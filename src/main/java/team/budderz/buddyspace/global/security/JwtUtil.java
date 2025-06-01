package team.budderz.buddyspace.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import team.budderz.buddyspace.infra.database.user.entity.UserRole;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String secretKey;

    private static final long ACCESS_EXP = 30 * 60 * 1000L; // 30분
    private static final long REFRESH_EXP = 7 * 24 * 60 * 60 * 1000L; // 7일

    // 토큰 생성
    public String createToken(Long userId, String role, String tokenType, long expireTime) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .claim("tokenType", tokenType)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public String createAccessToken(Long userId, UserRole role) {
        return createToken(userId, role.name(), "ACCESS", ACCESS_EXP);
    }

    public String createRefreshToken(Long userId, UserRole role) {
        return createToken(userId, role.name(), "REFRESH", REFRESH_EXP);
    }

    // 토큰 추출
    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")
                ? header.substring(7) : null);
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 인증 정보 추출 (UserAuth 객체)
    public UserAuth extractUserAuth(String token) {
        Claims body = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return new UserAuth(Long.parseLong(body.getSubject()), UserRole.valueOf(body.get("role", String.class)));
    }

    // 만료시간
    public Date getExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    // 토큰 타입
    public String getTokenType(String token) {
        return getClaims(token).get("tokenType", String.class);
    }

    public boolean isAccessToken(String token) {
        return "ACCESS".equals(getTokenType(token));
    }

    // Claims 추출
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

}
