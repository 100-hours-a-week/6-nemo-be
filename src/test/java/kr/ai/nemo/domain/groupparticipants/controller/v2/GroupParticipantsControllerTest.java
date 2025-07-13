package kr.ai.nemo.domain.groupparticipants.controller.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.messaging.GroupEventPublisher;
import kr.ai.nemo.domain.group.service.AiGroupService;
import kr.ai.nemo.domain.groupparticipants.exception.GroupParticipantErrorCode;
import kr.ai.nemo.domain.groupparticipants.exception.GroupParticipantException;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsCommandService;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.kafka.producer.KafkaNotifyGroupService;
import kr.ai.nemo.global.testUtil.MockMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GroupParticipantsController.class)
@MockMember
@Import({JwtProvider.class})
@ActiveProfiles("test")
@DisplayName("GroupParticipantsControllerV2 테스트")
class GroupParticipantsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private GroupParticipantsCommandService groupParticipantsCommandService;

  @MockitoBean
  private AiGroupService aiGroupService;

  @MockitoBean
  private KafkaNotifyGroupService kafkaNotifyGroupService;

  @MockitoBean
  private GroupEventPublisher groupEventPublisher;

  @MockitoBean
  private CustomUserDetailsService customUserDetailsService;

  @MockitoBean
  private UserRepository userRepository;

  // ===== 모임원 추방 테스트 =====

  @Test
  @DisplayName("[성공] 모임원 추방 테스트")
  void kickGroupParticipant_Success() throws Exception {
    // given
    Long groupId = 1L;
    Long targetUserId = 2L;

    doNothing().when(groupParticipantsCommandService).kickOut(eq(groupId), eq(targetUserId), any());
    doNothing().when(aiGroupService).notifyGroupLeft(targetUserId, groupId);
    doNothing().when(groupEventPublisher).publishGroupLeft(groupId, targetUserId);

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/{userId}", groupId, targetUserId)
            .with(csrf()))
        .andExpect(status().isNoContent());

    verify(groupParticipantsCommandService).kickOut(eq(groupId), eq(targetUserId), any());
  }

  @Test
  @DisplayName("[실패] 모임원 추방 - CSRF 토큰 없음")
  void kickGroupParticipant_NoCsrfToken_Forbidden() throws Exception {
    // given
    Long groupId = 1L;
    Long userId = 2L;

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/{userId}", groupId, userId))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("[실패] 모임원 추방 - 권한 없음 (리더가 아닌 경우)")
  void kickGroupParticipant_Forbidden() throws Exception {
    // given
    Long groupId = 1L;
    Long targetUserId = 2L;
    
    doThrow(new GroupException(GroupErrorCode.GROUP_KICK_FORBIDDEN))
        .when(groupParticipantsCommandService).kickOut(eq(groupId), eq(targetUserId), any());

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/{userId}", groupId, targetUserId)
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("[실패] 모임원 추방 - 존재하지 않는 모임원")
  void kickGroupParticipant_MemberNotFound() throws Exception {
    // given
    Long groupId = 1L;
    Long nonExistentUserId = 999L;
    
    doThrow(new GroupParticipantException(GroupParticipantErrorCode.NOT_GROUP_MEMBER))
        .when(groupParticipantsCommandService).kickOut(eq(groupId), eq(nonExistentUserId), any());

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/{userId}", groupId, nonExistentUserId)
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("[실패] 모임원 추방 - 존재하지 않는 모임")
  void kickGroupParticipant_GroupNotFound() throws Exception {
    // given
    Long nonExistentGroupId = 999L;
    Long targetUserId = 2L;
    
    doThrow(new GroupException(GroupErrorCode.GROUP_NOT_FOUND))
        .when(groupParticipantsCommandService).kickOut(eq(nonExistentGroupId), eq(targetUserId), any());

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/{userId}", nonExistentGroupId, targetUserId)
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("[실패] 모임원 추방 - 이미 추방된 사용자")
  void kickGroupParticipant_AlreadyKicked() throws Exception {
    // given
    Long groupId = 1L;
    Long targetUserId = 2L;
    
    doThrow(new GroupParticipantException(GroupParticipantErrorCode.ALREADY_KICKED_MEMBER))
        .when(groupParticipantsCommandService).kickOut(eq(groupId), eq(targetUserId), any());

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/{userId}", groupId, targetUserId)
            .with(csrf()))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("[실패] 모임원 추방 - 리더는 추방 불가")
  void kickGroupParticipant_LeaderCannotBeRemoved() throws Exception {
    // given
    Long groupId = 1L;
    Long leaderUserId = 1L; // 리더 사용자 ID
    
    doThrow(new GroupParticipantException(GroupParticipantErrorCode.LEADER_CANNOT_BE_REMOVED))
        .when(groupParticipantsCommandService).kickOut(eq(groupId), eq(leaderUserId), any());

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/{userId}", groupId, leaderUserId)
            .with(csrf()))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("[실패] 모임원 추방 - 잘못된 HTTP 메서드")
  void kickGroupParticipant_WrongMethod_MethodNotAllowed() throws Exception {
    // given
    Long groupId = 1L;
    Long userId = 2L;

    // when & then
    mockMvc.perform(get("/api/v2/groups/{groupId}/participants/{userId}", groupId, userId)
            .with(csrf()))
        .andExpect(status().isMethodNotAllowed());
  }

  // ===== 모임 탈퇴 테스트 =====

  @Test
  @DisplayName("[성공] 모임 탈퇴 테스트")
  void withdrawFromGroup_Success() throws Exception {
    // given
    Long groupId = 1L;
    Long userId = 1L; // MockMember에서 설정된 사용자 ID

    doNothing().when(groupParticipantsCommandService).withdrawGroup(eq(groupId), eq(userId));
    doNothing().when(aiGroupService).notifyGroupLeft(userId, groupId);
    doNothing().when(groupEventPublisher).publishGroupLeft(groupId, userId);

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/me", groupId)
            .with(csrf()))
        .andExpect(status().isNoContent());

    verify(groupParticipantsCommandService).withdrawGroup(eq(groupId), eq(userId));
  }

  @Test
  @DisplayName("[실패] 모임 탈퇴 - CSRF 토큰 없음")
  void withdrawFromGroup_NoCsrfToken_Forbidden() throws Exception {
    // given
    Long groupId = 1L;

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/me", groupId))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("[실패] 모임 탈퇴 - 리더는 탈퇴 불가")
  void withdrawFromGroup_LeaderCannotWithdraw() throws Exception {
    // given
    Long groupId = 1L;
    Long userId = 1L;
    
    doThrow(new GroupParticipantException(GroupParticipantErrorCode.LEADER_CANNOT_BE_REMOVED))
        .when(groupParticipantsCommandService).withdrawGroup(eq(groupId), eq(userId));

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/me", groupId)
            .with(csrf()))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("[실패] 모임 탈퇴 - 존재하지 않는 모임")
  void withdrawFromGroup_GroupNotFound() throws Exception {
    // given
    Long nonExistentGroupId = 999L;
    Long userId = 1L;
    
    doThrow(new GroupException(GroupErrorCode.GROUP_NOT_FOUND))
        .when(groupParticipantsCommandService).withdrawGroup(eq(nonExistentGroupId), eq(userId));

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/me", nonExistentGroupId)
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("[실패] 모임 탈퇴 - 이미 탈퇴한 모임")
  void withdrawFromGroup_AlreadyWithdrawn() throws Exception {
    // given
    Long groupId = 1L;
    Long userId = 1L;
    
    doThrow(new GroupParticipantException(GroupParticipantErrorCode.ALREADY_WITHDRAWN_MEMBER))
        .when(groupParticipantsCommandService).withdrawGroup(eq(groupId), eq(userId));

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/me", groupId)
            .with(csrf()))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("[실패] 모임 탈퇴 - 모임원이 아님")
  void withdrawFromGroup_NotGroupMember() throws Exception {
    // given
    Long groupId = 1L;
    Long userId = 1L;
    
    doThrow(new GroupParticipantException(GroupParticipantErrorCode.NOT_GROUP_MEMBER))
        .when(groupParticipantsCommandService).withdrawGroup(eq(groupId), eq(userId));

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/me", groupId)
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("[실패] 모임 탈퇴 - 잘못된 HTTP 메서드")
  void withdrawFromGroup_WrongMethod_MethodNotAllowed() throws Exception {
    // given
    Long groupId = 1L;

    // when & then
    mockMvc.perform(get("/api/v2/groups/{groupId}/participants/me", groupId)
            .with(csrf()))
        .andExpect(status().isMethodNotAllowed());
  }

  // ===== 경계값 및 예외 상황 테스트 =====

  @Test
  @DisplayName("[실패] 잘못된 그룹 ID 형식")
  void kickGroupParticipant_InvalidGroupIdFormat_BadRequest() throws Exception {
    // when & then
    mockMvc.perform(delete("/api/v2/groups/invalid-id/participants/1")
            .with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[실패] 잘못된 사용자 ID 형식")
  void kickGroupParticipant_InvalidUserIdFormat_BadRequest() throws Exception {
    // when & then
    mockMvc.perform(delete("/api/v2/groups/1/participants/invalid-id")
            .with(csrf()))
        .andExpect(status().isBadRequest());
  }
}
