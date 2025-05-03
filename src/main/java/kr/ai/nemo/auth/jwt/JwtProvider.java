package kr.ai.nemo.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

  @Value("${jwt.secret}")
  private String secretKeyString;

  @Value("${jwt.access-token-validity}")
  private long accessTokenValidity;

  @Value("${jwt.refresh-token-validity}")
  private long refreshTokenValidity;

  private Key secretKey;

  @PostConstruct
  public void init() {
    this.secretKey = new SecretKeySpec(
        secretKeyString.getBytes(StandardCharsets.UTF_8),
        SignatureAlgorithm.HS256.getJcaName()
    );
  }

  public String createAccessToken(Long userId) {
    return createToken(userId, accessTokenValidity);
  }

  public String createRefreshToken(Long userId) {
    return createToken(userId, refreshTokenValidity);
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
