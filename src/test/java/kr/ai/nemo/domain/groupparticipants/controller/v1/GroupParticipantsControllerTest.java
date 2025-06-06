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
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.dto.response.GroupParticipantsListResponse;
import kr.ai.nemo.domain.groupparticipants.dto.response.MyGroupDto;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsCommandService;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsQueryService;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import kr.ai.nemo.global.testUtil.MockMember;
import kr.ai.nemo.global.testUtil.TestReflectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = kr.ai.nemo.domain.groupparticipants.controller.v1.GroupParticipantsController.class)
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
  private CustomUserDetailsService customUserDetailsService;

  @Test
  @DisplayName("[성공] 모임 신청 API 테스트")
  void joinGroup_Success() throws Exception {
    // given
    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Long groupId = 1L;
    doNothing().when(groupParticipantsCommandService).applyToGroup(
        groupId, userDetails, Role.MEMBER, Status.JOINED);

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
        .andExpect(jsonPath("$.data.groups[0].name").value("테스트 모임"))
        .andExpect(jsonPath("$.data.groups[1].groupId").value(2L))
        .andExpect(jsonPath("$.data.groups[1].name").value("테스트 모임"));
  }
}
