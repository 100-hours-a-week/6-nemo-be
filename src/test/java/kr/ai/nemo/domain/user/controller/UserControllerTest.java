package kr.ai.nemo.domain.user.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.user.dto.UpdateUserImageRequest;
import kr.ai.nemo.domain.user.exception.UserErrorCode;
import kr.ai.nemo.global.testUtil.MockMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.ai.nemo.domain.user.dto.MyPageResponse;
import kr.ai.nemo.domain.user.dto.NicknameUpdateRequest;
import kr.ai.nemo.domain.user.service.UserService;

@WebMvcTest(UserController.class)
@MockMember
@Import(JwtProvider.class)
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
    @DisplayName("[성공] 마이페이지 조회")
    void getMyPage_Success() throws Exception {
        // given
        Long userId = 1L;
        LocalDateTime createdAt = LocalDateTime.now();
        MyPageResponse response = new MyPageResponse(
                "테스트유저",
                "test@example.com",
                "https://example.com/profile.jpg",
                createdAt
        );

        given(userService.getMyPage(userId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v2/users/me")
                        .header("userId", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("테스트유저"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/profile.jpg"));
    }

    @Test
    @DisplayName("[성공] 닉네임 업데이트")
    void updateNickname_Success() throws Exception {
        // given
        Long userId = 1L;
        String newNickname = "새로운닉네임";
        NicknameUpdateRequest request = new NicknameUpdateRequest(newNickname);
        LocalDateTime createdAt = LocalDateTime.now();
        
        MyPageResponse response = new MyPageResponse(
                newNickname,
                "test@example.com",
                "https://example.com/profile.jpg",
                createdAt
        );

        given(userService.updateMyNickname(eq(userId), any(NicknameUpdateRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v2/users/me/nickname")
                        .header("userId", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value(newNickname));
    }

    @Test
    @DisplayName("[실패] 닉네임 업데이트 - 빈 닉네임")
    void updateNickname_EmptyNickname_BadRequest() throws Exception {
        // given
        Long userId = 1L;
        NicknameUpdateRequest request = new NicknameUpdateRequest("");

        // when & then
        mockMvc.perform(patch("/api/v2/users/me/nickname")
                        .header("userId", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[성공] 프로필 이미지 업데이트 - base64")
    void updateProfileImage_Success_Base64() throws Exception {
        // given
        Long userId = 1L;
        String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD..."; // 일부 생략

        UpdateUserImageRequest request = new UpdateUserImageRequest(base64Image);

        LocalDateTime createdAt = LocalDateTime.now();
        MyPageResponse response = new MyPageResponse(
            "테스트유저",
            "test@example.com",
            "https://example.com/new-profile.jpg",
            createdAt
        );

        given(userService.updateUserImage(eq(userId), any()))
            .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v2/users/me/profile-image")
                .header("userId", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/new-profile.jpg"));
    }

    @Test
    @DisplayName("[실패] 프로필 이미지 업데이트 - 파일 없음")
    void updateProfileImage_NoFile_BadRequest() throws Exception {
        // given
        Long userId = 1L;

        // when & then
        mockMvc.perform(multipart("/api/v2/users/me/profile-image")
                        .header("userId", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
