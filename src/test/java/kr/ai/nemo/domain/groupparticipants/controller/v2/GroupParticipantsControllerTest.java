package kr.ai.nemo.domain.groupparticipants.controller.v2;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.groupparticipants.controller.v1.GroupParticipantsController;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsCommandService;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.testUtil.MockMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = kr.ai.nemo.domain.groupparticipants.controller.v2.GroupParticipantsController.class)
@MockMember
@Import(JwtProvider.class)
@ActiveProfiles("test")
@DisplayName("GroupParticipantsController 테스트")
class GroupParticipantsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private GroupParticipantsCommandService groupParticipantsCommandService;

  @MockitoBean
  private UserRepository userRepository;

  @Test
  @DisplayName("[성공] 모임원 추방 테스트")
  void kickGroupParticipant_Success() throws Exception {
    // given
    Long groupId = 1L;
    Long userId = 1L;
    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    // when
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/{userId}", groupId, userId)
          .with(csrf()))
        .andExpect(status().isNoContent());

    // then
    verify(groupParticipantsCommandService).kickOut(groupId, userId, userDetails);
  }

  @Test
  @DisplayName("[성공] 모임 탈퇴 테스트")
  void withdrawFromGroup_Success() throws Exception {
    // given
    Long groupId = 1L;
    Long userId = 1L;
    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    // when
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/me", groupId, userId)
        .with(csrf()))
        .andExpect(status().isNoContent());

    // then
    verify(groupParticipantsCommandService).withdrawGroup(groupId, userDetails.getUserId());
  }
}
