package kr.ai.nemo.common;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UriGenerator {

  @Value("${oauth.kakao.rest-api-key}")
  private String restApiKey;

  @Value("${oauth.kakao.redirect-uri}")
  private String redirectUri;

  public URI kakaoLogin(String state) {
    return UriComponentsBuilder
        .fromUriString("https://kauth.kakao.com/oauth/authorize")
        .queryParam("response_type", "code")
        .queryParam("client_id", restApiKey)
        .queryParam("redirect_uri", redirectUri)
        .queryParam("state", state)
        .build()
        .toUri();
  }

  public URI login(String state, String accessToken) {
    return URI.create(state + "?token=" + accessToken);
  }
}
