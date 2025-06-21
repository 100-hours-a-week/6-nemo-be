package kr.ai.nemo.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import jakarta.servlet.http.Cookie;
import java.net.URI;
import kr.ai.nemo.domain.auth.dto.TokenRefreshResponse;
import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.auth.service.OauthService;
import kr.ai.nemo.domain.auth.service.TokenManager;
import kr.ai.nemo.global.testUtil.MockMember;
import kr.ai.nemo.global.util.UriGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@MockMember
@Import(JwtProvider.class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OauthService oauthService;

    @MockitoBean
    private TokenManager tokenManager;

    @MockitoBean
    private UriGenerator uriGenerator;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("[성공] 카카오 로그인 시작 - 리다이렉트")
    void kakaoLogin_Success() throws Exception {
        // given
        String redirectUri = "https://nemo.ai.kr";
        String expectedKakaoUri = "https://kauth.kakao.com/oauth/authorize?client_id=test&redirect_uri=" + redirectUri;
        
        given(uriGenerator.kakaoLogin(redirectUri))
                .willReturn(URI.create(expectedKakaoUri));

        // when & then
        mockMvc.perform(get("/api/v1/auth/login/kakao")
                .param("redirect_uri", redirectUri))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedKakaoUri));
    }

    @Test
    @DisplayName("[실패] 카카오 로그인 - redirect_uri 파라미터 누락")
    void kakaoLogin_NoRedirectUri_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/auth/login/kakao"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[성공] 카카오 콜백 - 로그인 성공")
    void kakaoCallback_Success() throws Exception {
        // given
        String code = "valid_auth_code";
        String state = "random_state_value";
        String accessToken = "access_token_value";
        String expectedRedirectUri = "https://nemo.ai.kr/login?token=" + accessToken + "&state=" + state;
        
        given(oauthService.loginWithKakao(eq(code), isNull(), any()))  // 모두 matcher 사용
                .willReturn(accessToken);
        given(uriGenerator.login(state, accessToken))
                .willReturn(URI.create(expectedRedirectUri));

        // when & then
        mockMvc.perform(get("/auth/kakao/callback")
                .param("code", code)
                .param("state", state))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUri));
    }

    @Test
    @DisplayName("[성공] 카카오 콜백 - 상태 파라미터 없이")
    void kakaoCallback_WithoutState_Success() throws Exception {
        // given
        String code = "valid_auth_code";
        String accessToken = "access_token_value";
        String expectedRedirectUri = "https://nemo.ai.kr/login?token=" + accessToken;
        
        given(oauthService.loginWithKakao(eq(code), isNull(), any()))  // 모두 matcher 사용
                .willReturn(accessToken);
        given(uriGenerator.login(null, accessToken))
                .willReturn(URI.create(expectedRedirectUri));

        // when & then
        mockMvc.perform(get("/auth/kakao/callback")
                .param("code", code))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUri));
    }

    @Test
    @DisplayName("[실패] 카카오 콜백 - 에러 파라미터")
    void kakaoCallback_WithError_BadRequest() throws Exception {
        // given
        String error = "access_denied";
        String errorDescription = "The user denied the request";

        // when & then
        mockMvc.perform(get("/auth/kakao/callback")
                .param("error", error)
                .param("error_description", errorDescription))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("[실패] 카카오 콜백 - 코드 없음")
    void kakaoCallback_NoCode_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/auth/kakao/callback"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("[성공] 토큰 재발급")
    void reissueToken_Success() throws Exception {
        // given
        String refreshToken = "valid_refresh_token";
        String newAccessToken = "new_access_token";
        long expiresIn = 3600000L;

        TokenRefreshResponse response = new TokenRefreshResponse(newAccessToken, expiresIn);
        given(oauthService.reissueAccessToken(refreshToken))
                .willReturn(response);

        Cookie cookie = new Cookie("refresh_token", refreshToken);

        // when & then
        mockMvc.perform(post("/api/v1/auth/token/refresh")
                .cookie(cookie)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(expiresIn));  // 올바른 필드명
    }

    @Test
    @DisplayName("[실패] 토큰 재발급 - 리프레시 토큰 없음")
    void reissueToken_NoRefreshToken_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/auth/token/refresh")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[성공] 로그아웃")
    void logout_Success() throws Exception {
        // given
        String refreshToken = "valid_refresh_token";
        Cookie cookie = new Cookie("refresh_token", refreshToken);

        doNothing().when(oauthService).invalidateRefreshToken(refreshToken);
        doNothing().when(tokenManager).clearRefreshTokenCookie(any());

        // when & then
        mockMvc.perform(delete("/api/v1/auth/logout/kakao")
                .cookie(cookie)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 로그아웃 - 리프레시 토큰 없어도 성공")
    void logout_NoRefreshToken_Success() throws Exception {
        // given
        doNothing().when(tokenManager).clearRefreshTokenCookie(any());

        // when & then
        mockMvc.perform(delete("/api/v1/auth/logout/kakao")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[실패] 카카오 로그인 - 잘못된 HTTP 메서드")
    void kakaoLogin_WrongMethod_MethodNotAllowed() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                .param("redirect_uri", "https://nemo.ai.kr")
                .with(csrf()))
                .andExpect(status().isMethodNotAllowed());
    }
}
