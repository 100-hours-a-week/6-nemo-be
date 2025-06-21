package kr.ai.nemo.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.user.dto.MyPageResponse;
import kr.ai.nemo.domain.user.dto.NicknameUpdateRequest;
import kr.ai.nemo.domain.user.dto.NicknameUpdateResponse;
import kr.ai.nemo.domain.user.service.UserService;
import kr.ai.nemo.global.testUtil.MockMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@MockMember
@Import({JwtProvider.class})
@ActiveProfiles("test")
@DisplayName("UserController 테스트")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private CustomUserDetailsService customUserDetailsService;

  @Test
  @DisplayName("[성공] 마이페이지 조회 성공")
  void getMyPage_Success() throws Exception {
    // given
    MyPageResponse response = new MyPageResponse(
        "test",
        "test@example.com",
        "test.jpg",
        LocalDateTime.now()
    );

    given(userService.getMyPage(any()))
        .willReturn(response);

    // when & then
    mockMvc.perform(get("/api/v2/users/me")
          .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.nickname").value(response.nickname()))
        .andExpect(jsonPath("$.data.email").value(response.email()))
        .andExpect(jsonPath("$.data.profileImageUrl").value(response.profileImageUrl()));
  }

  @Test
  @DisplayName("[성공] 닉네임 변경 성공")
  void modifyMyNickname_Success() throws Exception {
    // given
    NicknameUpdateRequest request = new NicknameUpdateRequest(
        "nickname"
    );

    NicknameUpdateResponse response = new NicknameUpdateResponse(
        "nickname"
    );

    given(userService.updateMyNickname(any(), any()))
        .willReturn(response);

    // when & then
    mockMvc.perform(patch("/api/v2/users/me/nickname")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.nickname").value("nickname"));
  }

  @Test
  @DisplayName("[실패] 닉네임 변경 - 빈 닉네임")
  void modifyMyNickname_EmptyNickname_BadRequest() throws Exception {
    // given
    NicknameUpdateRequest request = new NicknameUpdateRequest("");

    // when & then
    mockMvc.perform(patch("/api/v2/users/me/nickname")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[실패] 닉네임 변경 - null 닉네임")
  void modifyMyNickname_NullNickname_BadRequest() throws Exception {
    // given
    NicknameUpdateRequest request = new NicknameUpdateRequest(null);

    // when & then
    mockMvc.perform(patch("/api/v2/users/me/nickname")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[실패] 닉네임 변경 - 너무 긴 닉네임")
  void modifyMyNickname_TooLongNickname_BadRequest() throws Exception {
    // given
    String longNickname = "a".repeat(21); // 21자 (제한 초과)
    NicknameUpdateRequest request = new NicknameUpdateRequest(longNickname);

    // when & then
    mockMvc.perform(patch("/api/v2/users/me/nickname")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[실패] 마이페이지 조회 - 인증되지 않은 사용자")
  void getMyPage_Unauthorized() throws Exception {
    // when & then
    mockMvc.perform(get("/api/v2/users/me")
            .with(csrf())
            .with(anonymous()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("[실패] 닉네임 변경 - 잘못된 Content-Type")
  void modifyMyNickname_InvalidContentType_BadRequest() throws Exception {
    // given
    NicknameUpdateRequest request = new NicknameUpdateRequest("newNickname");

    // when & then
    mockMvc.perform(patch("/api/v2/users/me/nickname")
          .with(csrf())
          .contentType(MediaType.TEXT_PLAIN) // 잘못된 Content-Type
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  @DisplayName("[실패] 닉네임 변경 - CSRF 토큰 없음")
  void modifyMyNickname_NoCsrfToken_Forbidden() throws Exception {
    // given
    NicknameUpdateRequest request = new NicknameUpdateRequest("newNickname");

    // when & then
    mockMvc.perform(patch("/api/v2/users/me/nickname")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }
}
