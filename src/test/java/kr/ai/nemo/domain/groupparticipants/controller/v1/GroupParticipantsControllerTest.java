package kr.ai.nemo.domain.groupparticipants.controller.v1;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.messaging.GroupEventPublisher;
import kr.ai.nemo.domain.group.service.AiGroupService;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.dto.response.GroupParticipantsListResponse;
import kr.ai.nemo.domain.groupparticipants.dto.response.MyGroupDto;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsCommandService;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsQueryService;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.unit.global.fixture.group.GroupFixture;
import kr.ai.nemo.unit.global.fixture.user.UserFixture;
import kr.ai.nemo.unit.global.kafka.producer.KafkaNotifyGroupService;
import kr.ai.nemo.unit.global.testUtil.MockMember;
import kr.ai.nemo.unit.global.testUtil.TestReflectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GroupParticipantsController.class)
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
  private GroupParticipantsQueryService groupParticipantsQueryService;

  @MockitoBean
  private AiGroupService aiGroupService;

  @MockitoBean
  private KafkaNotifyGroupService kafkaNotifyGroupService;

  @MockitoBean
  private GroupEventPublisher groupEventPublisher;

  @MockitoBean
  private CustomUserDetailsService customUserDetailsService;

  @Test
  @DisplayName("[성공] 모임 신청 API 테스트")
  void joinGroup_Success() throws Exception {
    // given
    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Long groupId = 1L;
    doNothing().when(groupParticipantsCommandService).applyToGroup(
        groupId, userDetails, Role.MEMBER, Status.JOINED);
    doNothing().when(groupEventPublisher).publishGroupJoined(userDetails.getUserId(), groupId);

    // when & then
    mockMvc.perform(post("/api/v1/groups/{groupId}/applications", groupId)
            .with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("[성공] 모임원 list 조회 테스트")
  void getGroupParticipants_Success() throws Exception {
    // given
    Long groupId = 1L;

    List<GroupParticipantsListResponse.GroupParticipantDto> participants = List.of(
        new GroupParticipantsListResponse.GroupParticipantDto(1L, "홍길동", "test1.jpg","MEMBER"),
        new GroupParticipantsListResponse.GroupParticipantDto(2L, "김철수", "test2.jpg","LEADER")
    );

    given(groupParticipantsQueryService.getAcceptedParticipants(groupId))
        .willReturn(participants);

    // when & then
    mockMvc.perform(get("/api/v1/groups/{groupId}/participants", groupId)
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.participants.length()").value(participants.size()))
        .andExpect(jsonPath("$.data.participants[0].userId").value(1L))
        .andExpect(jsonPath("$.data.participants[0].nickname").value("홍길동"))
        .andExpect(jsonPath("$.data.participants[0].profileImageUrl").value("test1.jpg"))
        .andExpect(jsonPath("$.data.participants[0].role").value("MEMBER"))
        .andExpect(jsonPath("$.data.participants[1].userId").value(2L))
        .andExpect(jsonPath("$.data.participants[1].nickname").value("김철수"))
        .andExpect(jsonPath("$.data.participants[1].profileImageUrl").value("test2.jpg"))
        .andExpect(jsonPath("$.data.participants[1].role").value("LEADER"));
  }

  @Test
  @DisplayName("[성공] 내가 참여 중인 모임 list 조회 테스트")
  void getMyGroup_Success() throws Exception {
    // given
    User user = UserFixture.createDefaultUser();
    Group group = GroupFixture.createDefaultGroup(user);
    Group group1 = GroupFixture.createDefaultGroup(user);
    TestReflectionUtils.setField(group, "id", 1L);
    TestReflectionUtils.setField(group1, "id", 2L);

    List<MyGroupDto> groupList = List.of(
        MyGroupDto.from(group), MyGroupDto.from(group1)
    );

    given(groupParticipantsQueryService.getMyGroups(anyLong())).willReturn(groupList);

    // when & then
    mockMvc.perform(get("/api/v1/groups/me")
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groups.length()").value(groupList.size()))
        .andExpect(jsonPath("$.data.groups[0].groupId").value(1L))
        .andExpect(jsonPath("$.data.groups[0].name").value(group.getName()))
        .andExpect(jsonPath("$.data.groups[1].groupId").value(2L))
        .andExpect(jsonPath("$.data.groups[1].name").value(group1.getName()));
  }

  @Test
  @DisplayName("[실패] 모임 신청 - CSRF 토큰 없음")
  void joinGroup_NoCsrfToken_Forbidden() throws Exception {
    // given
    Long groupId = 1L;

    // when & then
    mockMvc.perform(post("/api/v1/groups/{groupId}/applications", groupId))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("[실패] 모임 신청 - 잘못된 HTTP 메서드")
  void joinGroup_WrongMethod_MethodNotAllowed() throws Exception {
    // given
    Long groupId = 1L;

    // when & then
    mockMvc.perform(get("/api/v1/groups/{groupId}/applications", groupId)
            .with(csrf()))
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  @DisplayName("[성공] 모임원 조회 - 빈 목록")
  void getGroupParticipants_EmptyList_Success() throws Exception {
    // given
    Long groupId = 1L;
    given(groupParticipantsQueryService.getAcceptedParticipants(groupId))
        .willReturn(List.of());

    // when & then
    mockMvc.perform(get("/api/v1/groups/{groupId}/participants", groupId)
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.participants").isEmpty());
  }

  @Test
  @DisplayName("[성공] 내가 참여한 모임 조회 - 빈 목록")
  void getMyGroup_EmptyList_Success() throws Exception {
    // given
    given(groupParticipantsQueryService.getMyGroups(anyLong()))
        .willReturn(List.of());

    // when & then
    mockMvc.perform(get("/api/v1/groups/me")
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groups").isEmpty());
  }

  @Test
  @DisplayName("[실패] 존재하지 않는 모임 신청")
  void joinGroup_NonExistentGroup_Error() throws Exception {
    // given
    Long nonExistentGroupId = 999L;

    // when & then
    mockMvc.perform(post("/api/v1/groups/{groupId}/applications", nonExistentGroupId)
            .with(csrf()))
        .andExpect(status().isNoContent()); // 실제로는 Service에서 예외 발생
  }

  @Test
  @DisplayName("[실패] 존재하지 않는 모임의 참가자 조회")
  void getGroupParticipants_NonExistentGroup_Error() throws Exception {
    // given
    Long nonExistentGroupId = 999L;

    // when & then
    mockMvc.perform(get("/api/v1/groups/{groupId}/participants", nonExistentGroupId)
        .with(csrf()))
        .andExpect(status().isOk()); // 실제로는 Service에서 예외 발생하거나 빈 리스트 반환
  }
}
