package kr.ai.nemo.auth.service;

import java.time.LocalDateTime;

import kr.ai.nemo.auth.domain.UserToken;
import kr.ai.nemo.auth.domain.enums.DefaultUserValue;
import kr.ai.nemo.auth.domain.enums.LoginDevice;
import kr.ai.nemo.auth.domain.enums.OAuthProvider;
import kr.ai.nemo.auth.dto.KakaoTokenResponse;
import kr.ai.nemo.auth.dto.KakaoUserResponse;
import kr.ai.nemo.auth.dto.TokenRefreshResponse;
import kr.ai.nemo.auth.exception.OAuthErrorCode;
import kr.ai.nemo.auth.exception.OAuthException;
import kr.ai.nemo.auth.jwt.JwtProvider;
import kr.ai.nemo.user.domain.User;
import kr.ai.nemo.user.domain.enums.UserStatus;
import kr.ai.nemo.user.dto.UserDto;
import kr.ai.nemo.user.dto.UserLoginResponse;
import kr.ai.nemo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OauthService {

  private final RestTemplate restTemplate;
  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;
  private final UserTokenService userTokenService;

  @Value("${oauth.kakao.rest-api-key}")
  private String restApiKey;

  @Value("${oauth.kakao.redirect-uri}")
  private String redirectUri;

  @Transactional
  public UserLoginResponse loginWithKakao(String code) {
    try {
      KakaoTokenResponse kakaoToken = getAccessToken(code);
      if (kakaoToken.getAccessToken() == null || kakaoToken.getAccessToken().isEmpty()) {
        throw new OAuthException(OAuthErrorCode.EMPTY_ACCESS_TOKEN);
      }

      KakaoUserResponse userResponse = getUserInfo(kakaoToken.getAccessToken());
      User user = handleUser(userResponse);

      String accessToken = jwtProvider.createAccessToken(user.getId());
      String refreshToken = jwtProvider.createRefreshToken(user.getId());

      userTokenService.saveOrUpdateToken(user,
          OAuthProvider.KAKAO.name(), refreshToken, LoginDevice.WEB.name(), LocalDateTime.now().plusDays(30));

      return UserLoginResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .refreshTokenExpiresIn(jwtProvider.getRefreshTokenValidity())
          .user(UserDto.from(user))
          .build();
    } catch (OAuthException e) {
      throw e;
    } catch (Exception e) {
      throw new OAuthException(OAuthErrorCode.LOGIN_ERROR, e);
    }
  }

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
        throw new OAuthException(OAuthErrorCode.EMPTY_TOKEN_RESPONSE);
      }

      return response.getBody();
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
        throw new OAuthException(OAuthErrorCode.INVALID_CODE, e);
      } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        throw new OAuthException(OAuthErrorCode.INVALID_CLIENT, e);
      } else {
        throw new OAuthException(OAuthErrorCode.CLIENT_ERROR, e);
      }
    } catch (RestClientException e) {
      throw new OAuthException(OAuthErrorCode.CONNECTION_ERROR, e);
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
        throw new OAuthException(OAuthErrorCode.EMPTY_USER_INFO);
      }
      if (body.getId() == null) {
        throw new OAuthException(OAuthErrorCode.MISSING_USER_ID);
      }
      return body;
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        throw new OAuthException(OAuthErrorCode.INVALID_ACCESS_TOKEN, e);
      } else {
        throw new OAuthException(OAuthErrorCode.USER_INFO_ERROR, e);
      }
    } catch (RestClientException e) {
      throw new OAuthException(OAuthErrorCode.CONNECTION_ERROR, e);
    }
  }

  public User handleUser(KakaoUserResponse userResponse) {
    if (userResponse == null || userResponse.getId() == null) {
      throw new OAuthException(OAuthErrorCode.INVALID_USER_RESPONSE);
    }

    final String provider = OAuthProvider.KAKAO.name();
    final String providerId = userResponse.getId().toString();

    return userRepository.findByProviderAndProviderId(provider, providerId)
        .orElseGet(() -> userRepository.save(createUserFromResponse(userResponse)));
  }

  private User createUserFromResponse(KakaoUserResponse userResponse) {
    KakaoUserResponse.KakaoAccount account = userResponse.getKakaoAccount();
    KakaoUserResponse.Profile profile = (account != null) ? account.getProfile() : null;

    final String email = (account != null && account.getEmail() != null)
        ? account.getEmail()
        : DefaultUserValue.UNKNOWN_EMAIL;

    final String nickname = (profile != null && profile.getNickname() != null)
        ? profile.getNickname()
        : DefaultUserValue.UNKNOWN_NICKNAME;

    final String profileImageUrl = (profile != null && profile.getProfileImageUrl() != null)
        ? profile.getProfileImageUrl()
        : DefaultUserValue.DEFAULT_PROFILE_URL;

    return User.builder()
        .provider(OAuthProvider.KAKAO.name())
        .providerId(userResponse.getId().toString())
        .email(email)
        .nickname(nickname)
        .profileImageUrl(profileImageUrl)
        .status(UserStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  public TokenRefreshResponse reissueAccessToken(String refreshToken) {
    UserToken userToken = userTokenService.findValidToken(refreshToken);
    User user = userToken.getUser();

    String newAccessToken = jwtProvider.createAccessToken(user.getId());
    long expiresIn = jwtProvider.getAccessTokenValidity();

    return new TokenRefreshResponse(newAccessToken, expiresIn);
  }
}