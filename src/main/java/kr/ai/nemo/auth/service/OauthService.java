package kr.ai.nemo.auth.service;

import kr.ai.nemo.auth.dto.KakaoTokenResponse;
import kr.ai.nemo.auth.dto.KakaoUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OauthService {

  private final RestTemplate restTemplate;

  @Value("${oauth.kakao.rest-api-key}")
  private String restApiKey;

  @Value("${oauth.kakao.redirect-uri}")
  private String redirectUri;

  public KakaoTokenResponse getAccessToken(String code) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", restApiKey);
    params.add("redirect_uri", redirectUri);
    params.add("code", code);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(
        "https://kauth.kakao.com/oauth/token",
        request,
        KakaoTokenResponse.class
    );

    return response.getBody();
  }

  public KakaoUserResponse getUserInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
        "https://kapi.kakao.com/v2/user/me",
        HttpMethod.GET,
        request,
        KakaoUserResponse.class
    );

    return response.getBody();
  }

}
