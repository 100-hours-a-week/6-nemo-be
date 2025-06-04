package kr.ai.nemo.domain.auth.service;

import kr.ai.nemo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OauthService 테스트")
class OauthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KakaoOauthClient kakaoOauthClient;

    @Mock
    private TokenManager tokenManager;

    @InjectMocks
    private OauthService oauthService;

    @Test
    @DisplayName("카카오 OAuth 로그인 성공 테스트")
    void kakaoLogin_Success() {
        // given
        
        // when
        
        // then
    }
}
