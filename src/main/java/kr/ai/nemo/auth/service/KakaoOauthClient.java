package kr.ai.nemo.auth.service;

import kr.ai.nemo.auth.dto.KakaoTokenResponse;
import kr.ai.nemo.auth.dto.KakaoUserResponse;
import kr.ai.nemo.auth.exception.KakaoOAuthErrorCode;
import kr.ai.nemo.auth.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoOauthClient {
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
    try {
      ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(
          "https://kauth.kakao.com/oauth/token",
          request,
          KakaoTokenResponse.class
      );

      if (response.getBody() == null) {
        throw new AuthException(KakaoOAuthErrorCode.EMPTY_TOKEN_RESPONSE);
      }

      return response.getBody();
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
        throw new AuthException(KakaoOAuthErrorCode.INVALID_CODE);
      } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        throw new AuthException(KakaoOAuthErrorCode.INVALID_CLIENT);
      } else {
        throw new AuthException(KakaoOAuthErrorCode.CLIENT_ERROR);
      }
    } catch (RestClientException e) {
      throw new AuthException(KakaoOAuthErrorCode.CONNECTION_ERROR);
    }
  }

  public KakaoUserResponse getUserInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
          "https://kapi.kakao.com/v2/user/me",
          HttpMethod.GET,
          request,
          KakaoUserResponse.class
      );

      KakaoUserResponse body = response.getBody();

      if (body == null) {
        throw new AuthException(KakaoOAuthErrorCode.EMPTY_USER_INFO);
      }
      if (body.id() == null) {
        throw new AuthException(KakaoOAuthErrorCode.MISSING_USER_ID);
      }
      return body;
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        throw new AuthException(KakaoOAuthErrorCode.INVALID_ACCESS_TOKEN);
      } else {
        throw new AuthException(KakaoOAuthErrorCode.USER_INFO_ERROR);
      }
    } catch (RestClientException e) {
      throw new AuthException(KakaoOAuthErrorCode.CONNECTION_ERROR);
    }
  }
}
