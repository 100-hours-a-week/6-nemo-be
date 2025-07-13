package kr.ai.nemo.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import kr.ai.nemo.domain.auth.dto.KakaoUserResponse;
import kr.ai.nemo.domain.auth.exception.AuthException;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.unit.global.fixture.user.UserFixture;
import kr.ai.nemo.infra.ImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OauthUserService 테스트")
class OauthUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private OauthUserService oauthUserService;

    @Test
    @DisplayName("[성공] 기존 사용자 조회")
    void handleUser_ExistingUser_Success() {
        // given
        Long kakaoId = 123456789L;
        String provider = "KAKAO";
        String providerId = kakaoId.toString();
        
        KakaoUserResponse userResponse = new KakaoUserResponse(
                kakaoId,
                new KakaoUserResponse.KakaoAccount(
                        "test@example.com",
                        new KakaoUserResponse.Profile("testUser", "https://example.com/image.jpg", false)
                )
        );

        User existingUser = UserFixture.createUser("testUser", "test@example.com", provider, providerId);

        given(userRepository.findByProviderAndProviderId(provider, providerId))
                .willReturn(Optional.of(existingUser));

        // when
        User result = oauthUserService.handleUser(userResponse);

        // then
        assertThat(result).isEqualTo(existingUser);
        verify(userRepository).findByProviderAndProviderId(provider, providerId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("[성공] 새 사용자 생성 - 기본 이미지")
    void handleUser_NewUserWithDefaultImage_Success() {
        // given
        Long kakaoId = 123456789L;
        String provider = "KAKAO";
        String providerId = kakaoId.toString();
        
        KakaoUserResponse userResponse = new KakaoUserResponse(
                kakaoId,
                new KakaoUserResponse.KakaoAccount(
                        "test@example.com",
                        new KakaoUserResponse.Profile("testUser", "https://example.com/default.jpg", true)
                )
        );

        given(userRepository.findByProviderAndProviderId(provider, providerId))
                .willReturn(Optional.empty());
        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = oauthUserService.handleUser(userResponse);

        // then
        assertThat(result.getProvider()).isEqualTo(provider);
        assertThat(result.getProviderId()).isEqualTo(providerId);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getNickname()).isEqualTo("testUser");
        assertThat(result.getProfileImageUrl()).isEqualTo("https://example.com/default.jpg");
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);

        verify(userRepository).save(any(User.class));
        verify(imageService, never()).uploadKakaoProfileImage(anyString(), anyLong());
    }

    @Test
    @DisplayName("[성공] 새 사용자 생성 - 이메일 정보 없음")
    void handleUser_NewUserWithoutEmail_Success() {
        // given
        Long kakaoId = 123456789L;
        String provider = "KAKAO";
        String providerId = kakaoId.toString();
        
        KakaoUserResponse userResponse = new KakaoUserResponse(
                kakaoId,
                new KakaoUserResponse.KakaoAccount(
                        null,
                        new KakaoUserResponse.Profile("testUser", "https://example.com/image.jpg", true)
                )
        );

        given(userRepository.findByProviderAndProviderId(provider, providerId))
                .willReturn(Optional.empty());
        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = oauthUserService.handleUser(userResponse);

        // then
        assertThat(result.getProvider()).isEqualTo(provider);
        assertThat(result.getProviderId()).isEqualTo(providerId);
        assertThat(result.getEmail()).isEqualTo("unknown@example.com");
        assertThat(result.getNickname()).isEqualTo("testUser");
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("[성공] 새 사용자 생성 - 프로필 정보 없음")
    void handleUser_NewUserWithoutProfile_Success() {
        // given
        Long kakaoId = 123456789L;
        String provider = "KAKAO";
        String providerId = kakaoId.toString();
        
        KakaoUserResponse userResponse = new KakaoUserResponse(
                kakaoId,
                new KakaoUserResponse.KakaoAccount(
                        "test@example.com",
                        null // 프로필 없음
                )
        );

        given(userRepository.findByProviderAndProviderId(provider, providerId))
                .willReturn(Optional.empty());
        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = oauthUserService.handleUser(userResponse);

        // then
        assertThat(result.getProvider()).isEqualTo(provider);
        assertThat(result.getProviderId()).isEqualTo(providerId);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getNickname()).isEqualTo("알수없음"); // 기본값
        assertThat(result.getProfileImageUrl()).isNull();
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("[성공] 새 사용자 생성 - 계정 정보 없음")
    void handleUser_NewUserWithoutAccount_Success() {
        // given
        Long kakaoId = 123456789L;
        String provider = "KAKAO";
        String providerId = kakaoId.toString();
        
        KakaoUserResponse userResponse = new KakaoUserResponse(
                kakaoId,
                null // 계정 정보 없음
        );

        given(userRepository.findByProviderAndProviderId(provider, providerId))
                .willReturn(Optional.empty());
        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = oauthUserService.handleUser(userResponse);

        // then
        assertThat(result.getProvider()).isEqualTo(provider);
        assertThat(result.getProviderId()).isEqualTo(providerId);
        assertThat(result.getEmail()).isEqualTo("unknown@example.com"); // 기본값
        assertThat(result.getNickname()).isEqualTo("알수없음"); // 기본값
        assertThat(result.getProfileImageUrl()).isNull();
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("[실패] null 사용자 응답")
    void handleUser_NullUserResponse_ThrowException() {
        // when & then
        assertThatThrownBy(() -> oauthUserService.handleUser(null))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("[실패] null 사용자 ID")
    void handleUser_NullUserId_ThrowException() {
        // given
        KakaoUserResponse userResponse = new KakaoUserResponse(
                null,
                new KakaoUserResponse.KakaoAccount(
                        "test@example.com",
                        new KakaoUserResponse.Profile("testUser", "https://example.com/image.jpg", false)
                )
        );

        // when & then
        assertThatThrownBy(() -> oauthUserService.handleUser(userResponse))
                .isInstanceOf(AuthException.class);
    }
}
