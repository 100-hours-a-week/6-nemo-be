package kr.ai.nemo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.request.GroupCreateRequest;
import kr.ai.nemo.domain.group.messaging.GroupEventPublisher;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.dto.request.ScheduleCreateRequest;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.domain.scheduleparticipants.dto.ScheduleParticipantDecisionRequest;
import kr.ai.nemo.domain.scheduleparticipants.repository.ScheduleParticipantRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.unit.global.fixture.group.GroupFixture;
import kr.ai.nemo.unit.global.fixture.schedule.ScheduleFixture;
import kr.ai.nemo.unit.global.fixture.user.UserFixture;
import kr.ai.nemo.infra.ImageService;
import kr.ai.nemo.integration.common.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@DisplayName("모임 비즈니스 플로우 통합 테스트")
class GroupBusinessFlowIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private ScheduleRepository scheduleRepository;

  @Autowired
  private ScheduleParticipantRepository scheduleParticipantRepository;

  @MockitoBean
  private ImageService imageService;

  @MockitoBean
  private GroupEventPublisher groupEventPublisher;

  @AfterEach
  void cleanUp() {
    scheduleParticipantRepository.deleteAll();
    scheduleRepository.deleteAll();
    groupParticipantsRepository.deleteAll();
    groupRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("[통합 테스트] 모임 생성 → 멤버 참가 → 일정 생성 → 일정 참가 전체 플로우")
  void completeGroupLifecycle_IntegrationTest() throws Exception {
    // ===== Given: 사용자 준비 =====
    User leader = UserFixture.createUser("leader@test.com", "리더", "kakao", "123451");
    User member1 = UserFixture.createUser("member1@test.com", "멤버1", "kakao", "123452");
    User member2 = UserFixture.createUser("member2@test.com", "멤버2", "kakao", "123453");

    userRepository.saveAndFlush(leader);
    userRepository.saveAndFlush(member1);
    userRepository.saveAndFlush(member2);

    // Mock 설정
    given(imageService.uploadGroupImage(anyString())).willReturn("processed-image.jpg");
    willDoNothing().given(groupEventPublisher).publishGroupCreated(any());

    // ===== 1단계: 리더가 축구 모임 생성 =====
    GroupCreateRequest createRequest = new GroupCreateRequest(
        "주말 축구 모임",
        "축구 좋아하는 사람들 모여요",
        "매주 토요일 오후 축구를 합니다",
        "스포츠",
        "서울 올림픽공원",
        100,
        "soccer-group.jpg",
        List.of("축구", "운동", "주말"),
        "매주 토요일 오후 2시"
    );

    MvcResult createResult = mockMvc.perform(post("/api/v1/groups")
            .with(user(new CustomUserDetails(leader)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.name").value("주말 축구 모임"))
        .andExpect(jsonPath("$.data.currentUserCount").value(1)) // 리더만
        .andDo(print())
        .andReturn();

    String responseBody = createResult.getResponse().getContentAsString();
    Long groupId = ((Number) JsonPath.read(responseBody, "$.data.groupId")).longValue();

    // ===== 2단계: 멤버1이 모임 참가 =====
    mockMvc.perform(post("/api/v1/groups/{groupId}/applications", groupId)
            .with(user(new CustomUserDetails(member1))))
        .andExpect(status().isNoContent())
        .andDo(print());

    // ===== 3단계: 멤버2도 모임 참가 =====
    mockMvc.perform(post("/api/v1/groups/{groupId}/applications", groupId)
            .with(user(new CustomUserDetails(member2))))
        .andExpect(status().isNoContent())
        .andDo(print());

    // ===== 4단계: 모임 상태 확인 (3명: 리더 + 멤버2) =====
    mockMvc.perform(get("/api/v1/groups/{groupId}", groupId)
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.currentUserCount").value(3))
        .andExpect(jsonPath("$.data.name").value("주말 축구 모임"))
        .andDo(print());

    // ===== 5단계: 리더가 첫 번째 축구 일정 생성 =====
    ScheduleCreateRequest scheduleRequest = new ScheduleCreateRequest(
        groupId,
        "첫 번째 축구 경기",
        "1시간동안 축구 경기 진행합니다.",
        "올림픽공원 축구장",
        "A구역",
        LocalDateTime.now().plusDays(7)
    );

    MvcResult scheduleResult = mockMvc.perform(post("/api/v1/schedules")
            .with(user(new CustomUserDetails(leader)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(scheduleRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.title").value("첫 번째 축구 경기"))
        .andDo(print())
        .andReturn();

    String scheduleResponseBody = scheduleResult.getResponse().getContentAsString();
    Long scheduleId = ((Number) JsonPath.read(scheduleResponseBody,
        "$.data.scheduleId")).longValue();

    // ===== 6단계: 멤버1이 일정 참가 =====
    ScheduleParticipantDecisionRequest decisionRequest1 =
        new ScheduleParticipantDecisionRequest(ScheduleParticipantStatus.ACCEPTED);

    mockMvc.perform(patch("/api/v1/schedules/{scheduleId}/participants", scheduleId)
            .with(user(new CustomUserDetails(member1)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(decisionRequest1)))
        .andExpect(status().isOk())
        .andDo(print());

    // ===== 7단계: 멤버2도 일정 참가 =====
    ScheduleParticipantDecisionRequest decisionRequest2 =
        new ScheduleParticipantDecisionRequest(ScheduleParticipantStatus.ACCEPTED);

    mockMvc.perform(patch("/api/v1/schedules/{scheduleId}/participants", scheduleId)
            .with(user(new CustomUserDetails(member2)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(decisionRequest2)))
        .andExpect(status().isOk())
        .andDo(print());

    // ===== 8단계: 최종 검증 - 일정 참가자 확인 =====
    mockMvc.perform(get("/api/v1/schedules/{scheduleId}", scheduleId)
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.title").value("첫 번째 축구 경기"))
        .andDo(print());

    // ===== 데이터베이스 상태 검증 =====
    // 모임 참가자 수 확인
    List<GroupParticipants> participants = groupParticipantsRepository.findByGroupIdAndStatus(
        groupId, Status.JOINED);
    assertThat(participants).hasSize(3);
    assertThat(participants.stream().filter(p -> p.getRole() == Role.LEADER)).hasSize(1);
    assertThat(participants.stream().filter(p -> p.getRole() == Role.MEMBER)).hasSize(2);
    assertThat(participants.stream().allMatch(p -> p.getStatus() == Status.JOINED)).isTrue();

    // 일정 참가자 수 확인
    List<ScheduleParticipant> scheduleParticipants = scheduleParticipantRepository.findByScheduleId(
        scheduleId);
    assertThat(scheduleParticipants).hasSize(3);

    // 실제 모임 데이터 확인
    Optional<Group> groupOpt = groupRepository.findById(groupId);
    assertThat(groupOpt).isPresent();
    Group group = groupOpt.get();
    assertThat(group.getCurrentUserCount()).isEqualTo(3);
    assertThat(group.getName()).isEqualTo("주말 축구 모임");
  }

  @Test
  @DisplayName("[통합] 모임 삭제 시 연관 데이터 정리 플로우")
  void groupDeletionWithDataCleanup_IntegrationTest() throws Exception {
    // ===== Given: 복잡한 모임 구조 생성 =====
    User leader = UserFixture.createUser("leader@test.com", "리더", "kakao", "123451");
    User member = UserFixture.createUser("member1@test.com", "멤버1", "kakao", "123452");
    userRepository.saveAndFlush(leader);
    userRepository.saveAndFlush(member);

    // 모임 생성
    Group group = GroupFixture.createDefaultGroup(leader);
    group = groupRepository.saveAndFlush(group);

    // 멤버 추가
    GroupParticipants leaderParticipant = GroupParticipants.builder()
        .user(leader).group(group).role(Role.LEADER).status(Status.JOINED)
        .appliedAt(LocalDateTime.now()).build();
    GroupParticipants memberParticipant = GroupParticipants.builder()
        .user(member).group(group).role(Role.MEMBER).status(Status.JOINED)
        .appliedAt(LocalDateTime.now()).build();

    groupParticipantsRepository.saveAndFlush(leaderParticipant);
    groupParticipantsRepository.saveAndFlush(memberParticipant);

    // 일정 생성
    Schedule schedule = ScheduleFixture.createDefaultSchedule(leader, group);

    schedule = scheduleRepository.saveAndFlush(schedule);

    // 일정 참가자 추가
    ScheduleParticipant scheduleParticipant1 = ScheduleParticipant.builder()
        .schedule(schedule)
        .user(leader)
        .status(ScheduleParticipantStatus.ACCEPTED)
        .joinedAt(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .build();

    ScheduleParticipant scheduleParticipant2 = ScheduleParticipant.builder()
        .schedule(schedule)
        .user(member)
        .status(ScheduleParticipantStatus.ACCEPTED)
        .joinedAt(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .build();

    scheduleParticipantRepository.saveAndFlush(scheduleParticipant1);
    scheduleParticipantRepository.saveAndFlush(scheduleParticipant2);

    Long groupId = group.getId();
    Long scheduleId = schedule.getId();

    // Mock 설정
    willDoNothing().given(groupEventPublisher).publishGroupDeleted(any());

    // ===== When: 모임 삭제 실행 =====
    mockMvc.perform(delete("/api/v2/groups/{groupId}", groupId)
            .with(user(new CustomUserDetails(leader))))
        .andExpect(status().isNoContent())
        .andDo(print());

    // ===== Then: 연관 데이터 정리 확인 =====
    // 모임이 삭제되었는지 확인 (soft delete라면 status 확인)
    Optional<Group> deletedGroup = groupRepository.findById(groupId);

    deletedGroup.ifPresent(deletGroup ->
        assertThat(deletGroup.getStatus()).isEqualTo(GroupStatus.DISBANDED)
    );

    // 모임 참가자가 정리되었는지 확인
    List<GroupParticipants> remainingParticipants = groupParticipantsRepository.findByGroupIdAndStatus(
        groupId, Status.JOINED);
    assertThat(remainingParticipants).isEmpty();

    // 관련 일정이 정리되었는지 확인
    Optional<Schedule> remainingSchedule = scheduleRepository.findById(scheduleId);
    remainingSchedule.ifPresent(getSchedule ->
        assertThat(remainingSchedule.get().getStatus()).isEqualTo(ScheduleStatus.CANCELED)
    );

    // 일정 참가자가 정리되었는지 확인
    List<ScheduleParticipant> remainingScheduleParticipants =
        scheduleParticipantRepository.findByScheduleId(scheduleId);
    assertThat(remainingScheduleParticipants).isEmpty();
  }

  @Test
  @DisplayName("[통합] 멤버 탈퇴 시 연관 데이터 처리 플로우")
  void memberLeaveGroupFlow_IntegrationTest() throws Exception {
    // ===== Given: 모임 + 멤버 + 일정 구조 =====
    User leader = UserFixture.createUser("leader@test.com", "리더", "kakao", "123451");
    User member = UserFixture.createUser("member1@test.com", "멤버1", "kakao", "123452");
    userRepository.saveAndFlush(leader);
    userRepository.saveAndFlush(member);

    Group group = GroupFixture.createDefaultGroup(leader);
    group = groupRepository.saveAndFlush(group);

    GroupParticipants leaderParticipant = GroupParticipants.builder()
        .user(leader)
        .group(group)
        .role(Role.LEADER)
        .status(Status.JOINED)
        .appliedAt(LocalDateTime.now()).build();
    groupParticipantsRepository.saveAndFlush(leaderParticipant);

    // 멤버를 모임에 추가
    GroupParticipants memberParticipant = GroupParticipants.builder()
        .user(member)
        .group(group)
        .role(Role.MEMBER)
        .status(Status.JOINED)
        .appliedAt(LocalDateTime.now()).build();
    groupParticipantsRepository.saveAndFlush(memberParticipant);

    // 모임의 currentUserCount를 수동으로 동기화
    group.setCurrentUserCount(2);
    group = groupRepository.saveAndFlush(group);

    // 일정 생성 및 멤버 참가
    Schedule schedule = ScheduleFixture.createDefaultSchedule(leader, group);
    schedule = scheduleRepository.saveAndFlush(schedule);

    ScheduleParticipant scheduleParticipant = ScheduleParticipant.builder()
        .schedule(schedule)
        .user(member)
        .status(ScheduleParticipantStatus.ACCEPTED)
        .joinedAt(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .build();
    scheduleParticipantRepository.saveAndFlush(scheduleParticipant);

    Long groupId = group.getId();

    // ===== When: 멤버가 모임 탈퇴 =====
    mockMvc.perform(
            delete("/api/v2/groups/{groupId}/participants/me", groupId)
                .with(user(new CustomUserDetails(member))))
        .andExpect(status().isNoContent())
        .andDo(print());

    // ===== Then: 데이터 정리 확인 =====
    // 모임 참가자에서 제거되었는지 확인
    List<GroupParticipants> participants = groupParticipantsRepository.findByGroupIdAndStatus(
        groupId, Status.JOINED);
    assertThat(
        participants.stream().noneMatch(p -> p.getUser().getId().equals(member.getId()))).isTrue();

    // 모임 멤버 수 업데이트 확인
    Optional<Group> updatedGroup = groupRepository.findById(groupId);
    assertThat(updatedGroup).isPresent();
    assertThat(updatedGroup.get().getCurrentUserCount()).isEqualTo(1); // 리더만 남음
  }
}
