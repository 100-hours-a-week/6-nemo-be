package kr.ai.nemo.auth.service;

import java.time.LocalDateTime;
import kr.ai.nemo.auth.domain.enums.DefaultUserValue;
import kr.ai.nemo.auth.domain.enums.OAuthProvider;
import kr.ai.nemo.auth.dto.KakaoUserResponse;
import kr.ai.nemo.auth.exception.OAuthErrorCode;
import kr.ai.nemo.auth.exception.OAuthException;
import kr.ai.nemo.image.service.ImageService;
import kr.ai.nemo.user.domain.User;
import kr.ai.nemo.user.domain.enums.UserStatus;
import kr.ai.nemo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OauthUserService {
  private final UserRepository userRepository;
  private final ImageService imageService;

  @Transactional
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

    String profileImageUrl;
    if (profile.isDefaultImage()) {
      profileImageUrl = profile.getProfileImageUrl();
    } else {
      profileImageUrl = imageService.uploadKakaoProfileImage(profile.getProfileImageUrl(), userResponse.getId());
    }

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
}
