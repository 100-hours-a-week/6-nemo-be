package kr.ai.nemo.domain.auth.controller;

import kr.ai.nemo.domain.auth.service.OauthService;
import kr.ai.nemo.domain.auth.service.TokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OauthService oauthService;

    @MockitoBean
    private TokenManager tokenManager;

    @Test
    @DisplayName("카카오 로그인 API 테스트 - 구현 예정")
    void kakaoLogin_Success() {
        // given
        
        // when
        
        // then
        // 실제 API 테스트 구현 예정
    }
}
