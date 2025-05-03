package kr.ai.nemo.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  @Value("${jwt.secret}")
  private String secretKeyString;

  private Key secretKey;

  private static final long ACCESS_TOKEN_VALIDITY = 60 * 60 * 1000L;
  private static final long REFRESH_TOKEN_VALIDITY = 14 * 24 * 60 * 60 * 1000L;

  @PostConstruct
  public void init() {
    this.secretKey = new SecretKeySpec(
        secretKeyString.getBytes(StandardCharsets.UTF_8),
        SignatureAlgorithm.HS256.getJcaName()
    );
  }

  public String createAccessToken(Long userId) {
    return createToken(userId, ACCESS_TOKEN_VALIDITY);
  }

  public String createRefreshToken(Long userId) {
    return createToken(userId, REFRESH_TOKEN_VALIDITY);
  }

  private String createToken(Long userId, long validity) {
    Claims claims = Jwts.claims().setSubject(userId.toString());

    Date now = new Date();
    Date expiry = new Date(now.getTime() + validity);

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }
}
