package kr.ai.nemo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDateTime;
import java.util.List;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.dto.request.GroupCreateRequest;
import kr.ai.nemo.domain.group.dto.request.UpdateGroupImageRequest;
import kr.ai.nemo.domain.group.messaging.GroupEventPublisher;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.dto.request.ScheduleCreateRequest;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.domain.scheduleparticipants.dto.ScheduleParticipantDecisionRequest;
import kr.ai.nemo.domain.scheduleparticipants.repository.ScheduleParticipantRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.dto.NicknameUpdateRequest;
import kr.ai.nemo.global.fixture.schedule.ScheduleFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import kr.ai.nemo.infra.ImageService;
import kr.ai.nemo.integration.common.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@DisplayName("모임 비즈니스 플로우 확장 통합 테스트")
class GroupBusinessFlowExtendedIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private ScheduleRepository scheduleRepository;

  @Autowired
  private ScheduleParticipantRepository scheduleParticipantRepository;

  @MockitoBean
  private ImageService imageService;

  @MockitoBean
  private GroupEventPublisher groupEventPublisher;

  private User leader;
  private User member1;
  private User member2;
  private Group group;

  @BeforeEach
  void setUp() {
    // 기본 사용자 생성
    leader = UserFixture.createUser("leader@test.com", "모임장", "kakao", "123451");
    member1 = UserFixture.createUser("member1@test.com", "멤버1", "kakao", "123452");
    member2 = UserFixture.createUser("member2@test.com", "멤버2", "kakao", "123453");

    userRepository.save(leader);
    userRepository.save(member1);
    userRepository.save(member2);

    // Mock 설정
    given(imageService.uploadGroupImage(anyString())).willReturn("processed-image.jpg");
    given(imageService.updateImage(anyString(), anyString())).willReturn("updated-image.jpg");
    given(imageService.uploadKakaoProfileImage(anyString(), any())).willReturn("user-profile.jpg");
    doNothing().when(imageService).deleteImage(anyString());
    willDoNothing().given(groupEventPublisher).publishGroupCreated(any());
    willDoNothing().given(groupEventPublisher).publishGroupDeleted(any());
    willDoNothing().given(groupEventPublisher).publishGroupJoined(any(), any());
    willDoNothing().given(groupEventPublisher).publishGroupLeft(any(), any());
  }

  @AfterEach
  void cleanUp() {
    scheduleParticipantRepository.deleteAll();
    scheduleRepository.deleteAll();
    groupParticipantsRepository.deleteAll();
    groupRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("[통합] 모임 해체 - 나의 모임 목록과 일정 목록에서 조회 불가")
  void groupDisbandment_ShouldNotAppearInMyGroupsAndSchedules() throws Exception {
    // Given: 모임 생성 및 멤버 참가
    group = createGroupWithMembers();
    Schedule schedule = createScheduleWithParticipants(group);

    // When: 모임 해체
    mockMvc.perform(delete("/api/v2/groups/{groupId}", group.getId())
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isNoContent())
        .andDo(print());

    // Then: 나의 모임 목록에서 조회 안됨
    mockMvc.perform(get("/api/v1/groups/me")
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groups").isArray())
        .andExpect(jsonPath("$.data.groups[?(@.groupId == " + group.getId() + ")]").doesNotExist())
        .andDo(print());

    // Then: 나의 일정 목록에서 조회 안됨
    mockMvc.perform(get("/api/v1/schedules/me")
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.notResponded").isArray())
        .andExpect(jsonPath("$.data.notResponded.schedules[?(@.scheduleId == " + schedule.getId() + ")]").doesNotExist())
        .andDo(print());
  }

  @Test
  @DisplayName("[통합] 모임 대표 사진 변경 - 기존 S3 이미지 삭제 및 업데이트")
  void groupImageUpdate_ShouldDeleteOldImageAndUpdateNew() throws Exception {
    // Given: 모임 생성
    group = createGroupWithMembers();
    String newImageUrl = "https://new-image-url.com/new-image.jpg";

    UpdateGroupImageRequest updateRequest = new UpdateGroupImageRequest(newImageUrl);

    // When: 모임 이미지 업데이트
    mockMvc.perform(patch("/api/v2/groups/{groupId}/image", group.getId())
            .with(user(new CustomUserDetails(leader)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isNoContent())
        .andDo(print());

    // Then: 데이터베이스에서 업데이트 확인
    Group updatedGroup = groupRepository.findById(group.getId()).orElseThrow();
    assertThat(updatedGroup.getImageUrl()).isEqualTo("updated-image.jpg");
  }

  @Test
  @DisplayName("[통합] 모임 탈퇴 - 모임원 수 변화 및 재가입 테스트")
  void groupLeave_MemberCountChangeAndRejoin() throws Exception {
    // Given: 모임 생성 및 멤버 참가
    group = createGroupWithMembers();
    Long groupId = group.getId();

    // 초기 모임원 수 확인
    mockMvc.perform(get("/api/v1/groups/{groupId}", groupId)
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.currentUserCount").value(3))
        .andDo(print());

    // When: 멤버1이 탈퇴
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/me", groupId)
            .with(user(new CustomUserDetails(member1))))
        .andExpect(status().isNoContent())
        .andDo(print());

    // Then: 모임원 수 감소 확인
    mockMvc.perform(get("/api/v1/groups/{groupId}", groupId)
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.currentUserCount").value(2))
        .andDo(print());

    // When: 멤버1이 재가입
    mockMvc.perform(post("/api/v1/groups/{groupId}/applications", groupId)
            .with(user(new CustomUserDetails(member1))))
        .andExpect(status().isNoContent())
        .andDo(print());

    // Then: 모임원 수 증가 확인
    mockMvc.perform(get("/api/v1/groups/{groupId}", groupId)
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.currentUserCount").value(3))
        .andDo(print());
  }

  @Test
  @DisplayName("[통합] 모임 탈퇴 후 일정 신청/생성 불가능")
  void afterGroupLeave_CannotCreateOrJoinSchedules() throws Exception {
    // Given: 모임 생성
    group = createGroupWithMembers();
    Long groupId = group.getId();

    // When: 멤버1이 탈퇴
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/me", groupId)
            .with(user(new CustomUserDetails(member1))))
        .andExpect(status().isNoContent())
        .andDo(print());

    // Then: 탈퇴한 멤버는 일정 생성 불가능 (모임 참가자가 아니므로 404)
    ScheduleCreateRequest scheduleRequest = new ScheduleCreateRequest(
        groupId,
        "새로운 일정",
        "설명",
        "장소",
        "상세 장소",
        LocalDateTime.now().plusDays(1)
    );

    mockMvc.perform(post("/api/v1/schedules")
            .with(user(new CustomUserDetails(member1)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(scheduleRequest)))
        .andExpect(status().isNotFound())
        .andDo(print());

    // Given: 기존 일정이 있는 경우
    Schedule schedule = createScheduleWithParticipants(group);

    // Then: 탈퇴한 멤버는 일정 참가 불가능
    ScheduleParticipantDecisionRequest decisionRequest =
        new ScheduleParticipantDecisionRequest(ScheduleParticipantStatus.ACCEPTED);

    mockMvc.perform(patch("/api/v1/schedules/{scheduleId}/participants", schedule.getId())
            .with(user(new CustomUserDetails(member1)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(decisionRequest)))
        .andExpect(status().isForbidden())
        .andDo(print());
  }

  @Test
  @DisplayName("[통합] 모임원 추방 - 모임장만 추방 가능")
  void memberKick_OnlyLeaderCanKickMembers() throws Exception {
    // Given: 모임 생성
    group = createGroupWithMembers();
    Long groupId = group.getId();

    // When: 일반 멤버가 다른 멤버 추방 시도 (403 Forbidden)
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/{userId}", groupId, member2.getId())
            .with(user(new CustomUserDetails(member1))))
        .andExpect(status().isForbidden())
        .andDo(print());

    // When: 모임장이 멤버 추방
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/{userId}", groupId, member1.getId())
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isNoContent())
        .andDo(print());

    // Then: 모임원 수 감소 확인
    mockMvc.perform(get("/api/v1/groups/{groupId}", groupId)
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.currentUserCount").value(2))
        .andDo(print());

    // Then: 추방된 멤버의 나의 모임 목록에서 조회 안됨
    mockMvc.perform(get("/api/v1/groups/me")
            .with(user(new CustomUserDetails(member1))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groups[?(@.groupId == " + groupId + ")]").doesNotExist())
        .andDo(print());
  }

  @Test
  @DisplayName("[통합] 추방된 모임원 일정 신청/생성 불가능")
  void kickedMember_CannotCreateOrJoinSchedules() throws Exception {
    // Given: 모임 생성 및 멤버 추방
    group = createGroupWithMembers();
    Long groupId = group.getId();

    // 모임장이 멤버1 추방
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/{userId}", groupId, member1.getId())
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isNoContent())
        .andDo(print());

    // Then: 추방된 멤버는 일정 생성 불가능
    ScheduleCreateRequest scheduleRequest = new ScheduleCreateRequest(
        groupId,
        "추방된 멤버의 일정",
        "설명",
        "장소",
        "상세 장소",
        LocalDateTime.now().plusDays(1)
    );

    mockMvc.perform(post("/api/v1/schedules")
            .with(user(new CustomUserDetails(member1)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(scheduleRequest)))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("[통합] 마이페이지 조회")
  void myPage_ShouldLoadSuccessfully() throws Exception {
    // When & Then: 마이페이지 조회
    mockMvc.perform(get("/api/v2/users/me")
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.nickname").value(leader.getNickname()))
        .andExpect(jsonPath("$.data.email").value(leader.getEmail()))
        .andDo(print());
  }

  @Test
  @DisplayName("[통합] 닉네임 수정 - 중복 닉네임 변경 불가")
  void nicknameUpdate_DuplicateNicknameNotAllowed() throws Exception {
    // Given: 기존 닉네임
    String duplicateNickname = member1.getNickname();

    NicknameUpdateRequest updateRequest = new NicknameUpdateRequest(duplicateNickname);

    // When & Then: 중복 닉네임으로 변경 시도
    mockMvc.perform(patch("/api/v2/users/me/nickname")
            .with(user(new CustomUserDetails(leader)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isConflict())
        .andDo(print());

    String newNickname = "새로운닉네임";

    // Given: 유니크한 닉네임
    NicknameUpdateRequest validUpdateRequest = new NicknameUpdateRequest(newNickname);

    // When & Then: 유니크한 닉네임으로 변경 성공
    mockMvc.perform(patch("/api/v2/users/me/nickname")
            .with(user(new CustomUserDetails(leader)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validUpdateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.nickname").value(newNickname))
        .andDo(print());
  }

  @Test
  @DisplayName("[통합] 종료된 일정 참여 불가능")
  void completedSchedule_CannotJoin() throws Exception {
    // Given: 모임 생성
    group = createGroupWithMembers();

    // Given: 완료된 일정을 직접 생성하여 저장
    Schedule schedule = ScheduleFixture.createDefaultSchedule(leader, group);
    schedule = scheduleRepository.saveAndFlush(schedule);

    // 일정을 완료 상태로 변경
    schedule.complete();
    schedule = scheduleRepository.saveAndFlush(schedule);

    // 멤버1을 일정 참가자로 추가 (PENDING 상태)
    ScheduleParticipant scheduleParticipant = ScheduleParticipant.builder()
        .schedule(schedule)
        .user(member1)
        .status(ScheduleParticipantStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    scheduleParticipantRepository.saveAndFlush(scheduleParticipant);

    // When & Then: 완료된 일정 참가 시도
    ScheduleParticipantDecisionRequest decisionRequest =
        new ScheduleParticipantDecisionRequest(ScheduleParticipantStatus.ACCEPTED);

    mockMvc.perform(patch("/api/v1/schedules/{scheduleId}/participants", schedule.getId())
            .with(user(new CustomUserDetails(member1)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(decisionRequest)))
        .andExpect(status().isConflict())
        .andDo(print());
  }

  @Test
  @DisplayName("[통합] 모임 재가입 후 상태값 정상 변경")
  void groupRejoin_StatusShouldBeUpdatedCorrectly() throws Exception {
    // Given: 모임 생성 및 탈퇴
    group = createGroupWithMembers();
    Long groupId = group.getId();

    // 멤버1 탈퇴
    mockMvc.perform(delete("/api/v2/groups/{groupId}/participants/me", groupId)
            .with(user(new CustomUserDetails(member1))))
        .andExpect(status().isNoContent())
        .andDo(print());

    // When: 재가입
    mockMvc.perform(post("/api/v1/groups/{groupId}/applications", groupId)
            .with(user(new CustomUserDetails(member1))))
        .andExpect(status().isNoContent())
        .andDo(print());

    // Then: 상태값 확인
    List<GroupParticipants> participants = groupParticipantsRepository
        .findByGroupIdAndStatus(groupId, Status.JOINED);

    // 총 3명 (리더 + 멤버1 재가입 + 멤버2)
    assertThat(participants).hasSize(3);

    // 멤버1의 상태 확인
    GroupParticipants rejoinedMember = participants.stream()
        .filter(p -> p.getUser().getId().equals(member1.getId()))
        .findFirst()
        .orElseThrow();

    assertThat(rejoinedMember.getStatus()).isEqualTo(Status.JOINED);
    assertThat(rejoinedMember.getRole()).isEqualTo(Role.MEMBER);
  }

  private Group createGroupWithMembers() throws Exception {
    // 모임 생성
    GroupCreateRequest createRequest = new GroupCreateRequest(
        "테스트 모임",
        "테스트 모임입니다",
        "상세 설명",
        "스포츠",
        "서울",
        100,
        "test-image.jpg",
        List.of("테스트"),
        "정기 일정"
    );

    MvcResult createResult = mockMvc.perform(post("/api/v1/groups")
            .with(user(new CustomUserDetails(leader)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated())
        .andDo(print())
        .andReturn();

    String responseBody = createResult.getResponse().getContentAsString();
    Long groupId = ((Number) JsonPath.read(responseBody, "$.data.groupId")).longValue();

    // 멤버들 참가
    mockMvc.perform(post("/api/v1/groups/{groupId}/applications", groupId)
            .with(user(new CustomUserDetails(member1))))
        .andExpect(status().isNoContent())
        .andDo(print());

    mockMvc.perform(post("/api/v1/groups/{groupId}/applications", groupId)
            .with(user(new CustomUserDetails(member2))))
        .andExpect(status().isNoContent())
        .andDo(print());

    return groupRepository.findById(groupId).orElseThrow();
  }

  private Schedule createScheduleWithParticipants(Group group) throws Exception {
    ScheduleCreateRequest scheduleRequest = new ScheduleCreateRequest(
        group.getId(),
        "테스트 일정",
        "테스트 일정입니다",
        "테스트 장소",
        "상세 장소",
        LocalDateTime.now().plusDays(1)
    );

    MvcResult scheduleResult = mockMvc.perform(post("/api/v1/schedules")
            .with(user(new CustomUserDetails(leader)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(scheduleRequest)))
        .andExpect(status().isCreated())
        .andDo(print())
        .andReturn();

    String responseBody = scheduleResult.getResponse().getContentAsString();
    Long scheduleId = ((Number) JsonPath.read(responseBody, "$.data.scheduleId")).longValue();

    return scheduleRepository.findById(scheduleId).orElseThrow();
  }
}
