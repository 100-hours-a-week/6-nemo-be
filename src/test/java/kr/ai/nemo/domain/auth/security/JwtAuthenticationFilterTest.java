package kr.ai.nemo.domain.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;
import kr.ai.nemo.domain.user.exception.UserErrorCode;
import kr.ai.nemo.domain.user.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider, customUserDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("쿠키가 없는 경우 정상 통과")
    void doFilterInternal_NoCookies() throws ServletException, IOException {
        // given
        when(request.getCookies()).thenReturn(null);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("access_token 쿠키가 없는 경우 정상 통과")
    void doFilterInternal_NoAccessTokenCookie() throws ServletException, IOException {
        // given
        Cookie[] cookies = {new Cookie("other_cookie", "value")};
        when(request.getCookies()).thenReturn(cookies);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("유효한 토큰으로 인증 성공")
    void doFilterInternal_ValidToken() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        Long userId = 1L;
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .nickname("testUser")
            .status(UserStatus.ACTIVE)
            .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        Cookie[] cookies = {new Cookie("access_token", token)};
        when(request.getCookies()).thenReturn(cookies);
        when(jwtProvider.validateToken(token)).thenReturn(true);
        when(jwtProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(customUserDetailsService.loadUserById(userId)).thenReturn(userDetails);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);
    }

    @Test
    @DisplayName("[실패] 만료된 토큰으로 인증 실패")
    void doFilterInternal_ExpiredToken() throws ServletException, IOException {
        // given
        String token = "expired.jwt.token";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        Cookie[] cookies = {new Cookie("access_token", token)};
        when(request.getCookies()).thenReturn(cookies);
        when(jwtProvider.validateToken(token)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));
        when(response.getWriter()).thenReturn(printWriter);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(401);
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(filterChain, never()).doFilter(request, response);
        String responseBody = stringWriter.toString();
        assertThat(responseBody).contains("401");
        assertThat(responseBody).contains("토큰이 만료되었습니다");
    }

    @Test
    @DisplayName("잘못된 토큰으로 인증 실패")
    void doFilterInternal_InvalidToken() throws ServletException, IOException {
        // given
        String token = "invalid.jwt.token";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        Cookie[] cookies = {new Cookie("access_token", token)};
        when(request.getCookies()).thenReturn(cookies);
        when(jwtProvider.validateToken(token)).thenThrow(new JwtException("Invalid token"));
        when(response.getWriter()).thenReturn(printWriter);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(401);
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("[실패] 탈퇴한 사용자 인증 실패")
    void doFilterInternal_WithdrawnUser() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        Long userId = 1L;
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        Cookie[] cookies = {new Cookie("access_token", token)};
        when(request.getCookies()).thenReturn(cookies);
        when(jwtProvider.validateToken(token)).thenReturn(true);
        when(jwtProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(customUserDetailsService.loadUserById(userId))
            .thenThrow(new UserException(UserErrorCode.USER_WITHDRAWN));
        when(response.getWriter()).thenReturn(printWriter);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(409); // CONFLICT
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 인증 실패")
    void doFilterInternal_UserNotFound() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        Long userId = 1L;
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        Cookie[] cookies = {new Cookie("access_token", token)};
        when(request.getCookies()).thenReturn(cookies);
        when(jwtProvider.validateToken(token)).thenReturn(true);
        when(jwtProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(customUserDetailsService.loadUserById(userId))
            .thenThrow(new UserException(UserErrorCode.USER_NOT_FOUND));
        when(response.getWriter()).thenReturn(printWriter);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(404);
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("여러 쿠키 중 access_token 찾기")
    void doFilterInternal_FindAccessTokenAmongMultipleCookies() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        Long userId = 1L;
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .nickname("testUser")
            .status(UserStatus.ACTIVE)
            .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        Cookie[] cookies = {
            new Cookie("session_id", "session123"),
            new Cookie("access_token", token),
            new Cookie("refresh_token", "refresh123")
        };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtProvider.validateToken(token)).thenReturn(true);
        when(jwtProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(customUserDetailsService.loadUserById(userId)).thenReturn(userDetails);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }
}
