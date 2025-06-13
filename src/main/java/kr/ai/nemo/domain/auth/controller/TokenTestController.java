package kr.ai.nemo.domain.auth.controller;

import kr.ai.nemo.domain.auth.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test/token")
@RequiredArgsConstructor
@Profile({ "local", "dev"})
public class TokenTestController {

  private final JwtProvider jwtProvider;

  // http://localhost:8080/test/token/access?userId=1
  @GetMapping("/access")
  public String generateAccessToken(@RequestParam Long userId) {
    return jwtProvider.createAccessToken(userId);
  }

  // http://localhost:8080/test/token/refresh?userId=1
  @GetMapping("/refresh")
  public String generateRefreshToken(@RequestParam Long userId) {
    return jwtProvider.createRefreshToken(userId);
  }
}
