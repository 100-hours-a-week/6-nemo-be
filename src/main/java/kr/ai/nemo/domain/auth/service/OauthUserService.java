package kr.ai.nemo.domain.auth.service;

import java.time.LocalDateTime;
import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.domain.enums.DefaultUserValue;
import kr.ai.nemo.domain.auth.domain.enums.OAuthProvider;
import kr.ai.nemo.domain.auth.dto.KakaoUserResponse;
import kr.ai.nemo.domain.auth.exception.KakaoOAuthErrorCode;
import kr.ai.nemo.domain.auth.exception.AuthException;
import kr.ai.nemo.infra.ImageService;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;
import kr.ai.nemo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OauthUserService {
  private final UserRepository userRepository;
  private final ImageService imageService;

  @TimeTrace
  @Transactional
  public User handleUser(KakaoUserResponse userResponse) {
    if (userResponse == null || userResponse.id() == null) {
      throw new AuthException(KakaoOAuthErrorCode.INVALID_USER_RESPONSE);
    }

    final String provider = OAuthProvider.KAKAO.name();
    final String providerId = userResponse.id().toString();

    return userRepository.findByProviderAndProviderId(provider, providerId)
        .orElseGet(() -> userRepository.save(createUserFromResponse(userResponse)));
  }

  @TimeTrace
  private User createUserFromResponse(KakaoUserResponse userResponse) {
    KakaoUserResponse.KakaoAccount account = userResponse.kakaoAccount();
    KakaoUserResponse.Profile profile = (account != null) ? account.profile() : null;

    final String email = (account != null && account.email() != null)
        ? account.email()
        : DefaultUserValue.UNKNOWN_EMAIL;

    final String nickname = (profile != null && profile.nickname() != null)
        ? profile.nickname()
        : DefaultUserValue.UNKNOWN_NICKNAME;

    String profileImageUrl;
    if (profile.isDefaultImage()) {
      profileImageUrl = profile.profileImageUrl();
    } else {
      profileImageUrl = imageService.uploadKakaoProfileImage(profile.profileImageUrl(), userResponse.id());
    }

    return User.builder()
        .provider(OAuthProvider.KAKAO.name())
        .providerId(userResponse.id().toString())
        .email(email)
        .nickname(nickname)
        .profileImageUrl(profileImageUrl)
        .status(UserStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }
}
