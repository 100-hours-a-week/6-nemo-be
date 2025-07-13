package kr.ai.nemo.domain.group.controller.v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.group.dto.request.GroupCreateRequest;
import kr.ai.nemo.domain.group.dto.request.GroupGenerateRequest;
import kr.ai.nemo.domain.group.dto.response.GroupCreateResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDetailResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDto;
import kr.ai.nemo.domain.group.dto.response.GroupGenerateResponse;
import kr.ai.nemo.domain.group.dto.response.GroupListResponse;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.messaging.GroupEventPublisher;
import kr.ai.nemo.domain.group.service.AiGroupService;
import kr.ai.nemo.domain.group.service.GroupCommandService;
import kr.ai.nemo.domain.group.service.GroupQueryService;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleListResponse;
import kr.ai.nemo.domain.schedule.service.ScheduleQueryService;
import kr.ai.nemo.unit.global.kafka.producer.KafkaNotifyGroupService;
import kr.ai.nemo.unit.global.testUtil.MockMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GroupController.class)
@MockMember
@Import(JwtProvider.class)
@ActiveProfiles("test")
@DisplayName("GroupControllerV1 테스트")
class GroupControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private GroupCommandService groupCommandService;

  @MockitoBean
  private GroupQueryService groupQueryService;

  @MockitoBean
  private ScheduleQueryService scheduleQueryService;

  @MockitoBean
  private AiGroupService aiGroupService;

  @MockitoBean
  private KafkaNotifyGroupService kafkaNotifyGroupService;

  @MockitoBean
  private GroupEventPublisher groupEventPublisher;

  @MockitoBean
  private CustomUserDetailsService customUserDetailsService;

  // ===== 모임 조회 테스트 =====

  @Test
  @DisplayName("[성공] 모임 리스트 조회 API 테스트")
  void getAllGroups_Success() throws Exception {
    // given
    List<GroupDto> groupList = List.of(
        GroupDto.builder()
            .groupId(1L)
            .name("자바 스터디")
            .category("IT/개발")
            .summary("자바 마스터를 위한 스터디")
            .location("서울")
            .currentUserCount(5)
            .maxUserCount(10)
            .imageUrl("image1.jpg")
            .tags(List.of("자바", "백엔드"))
            .build(),

        GroupDto.builder()
            .groupId(2L)
            .name("운동 모임")
            .category("스포츠")
            .summary("건강한 삶을 위한 운동")
            .location("부산")
            .currentUserCount(3)
            .maxUserCount(8)
            .imageUrl("image2.jpg")
            .tags(List.of("헬스", "달리기"))
            .build()
    );

    Page<GroupDto> mockPage = new PageImpl<>(groupList, PageRequest.of(0, 10), groupList.size());
    given(groupQueryService.getGroups(any(), any())).willReturn(GroupListResponse.from(mockPage));

    // when & then
    mockMvc.perform(get("/api/v1/groups")
            .param("sort", "createdAt")
            .param("page", "0")
            .param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groups[0].groupId").value(1))
        .andExpect(jsonPath("$.data.groups[0].name").value("자바 스터디"))
        .andExpect(jsonPath("$.data.groups[0].tags[0]").value("자바"))
        .andExpect(jsonPath("$.data.totalPages").value(1))
        .andExpect(jsonPath("$.data.totalElements").value(2))
        .andExpect(jsonPath("$.data.pageNumber").value(0))
        .andExpect(jsonPath("$.data.isLast").value(true));
  }

  @Test
  @DisplayName("[성공] 카테고리별 모임 검색")
  void getGroupsByCategory_Success() throws Exception {
    // given
    List<GroupDto> groupList = List.of(
        GroupDto.builder()
            .groupId(1L)
            .name("자바 스터디")
            .category("IT/개발")
            .summary("자바 마스터를 위한 스터디")
            .location("서울")
            .currentUserCount(5)
            .maxUserCount(10)
            .imageUrl("image1.jpg")
            .tags(List.of("자바", "백엔드"))
            .build()
    );

    Page<GroupDto> mockPage = new PageImpl<>(groupList, PageRequest.of(0, 10), groupList.size());
    given(groupQueryService.getGroups(any(), any())).willReturn(GroupListResponse.from(mockPage));

    // when & then
    mockMvc.perform(get("/api/v1/groups/search")
            .param("category", "IT/개발")
            .param("sort", "createdAt")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groups[0].groupId").value(1))
        .andExpect(jsonPath("$.data.groups[0].name").value("자바 스터디"))
        .andExpect(jsonPath("$.data.groups[0].category").value("IT/개발"));
  }

  @Test
  @DisplayName("[성공] 키워드별 모임 검색")
  void getGroupsByKeyword_Success() throws Exception {
    // given
    List<GroupDto> groupList = List.of(
        GroupDto.builder()
            .groupId(1L)
            .name("자바 스터디")
            .category("IT/개발")
            .summary("자바 마스터를 위한 스터디")
            .location("서울")
            .currentUserCount(5)
            .maxUserCount(10)
            .imageUrl("image1.jpg")
            .tags(List.of("자바", "백엔드"))
            .build()
    );

    Page<GroupDto> mockPage = new PageImpl<>(groupList, PageRequest.of(0, 10), groupList.size());
    given(groupQueryService.getGroups(any(), any())).willReturn(GroupListResponse.from(mockPage));

    // when & then
    mockMvc.perform(get("/api/v1/groups/search")
            .param("keyword", "자바")
            .param("sort", "createdAt")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groups[0].name").value("자바 스터디"));
  }

  @Test
  @DisplayName("[성공] 모임 상세 조회")
  void getGroupDetail_Success() throws Exception {
    // given
    GroupDetailResponse mockResponse = new GroupDetailResponse(
        "테스트 모임",
        "IT/개발",
        "테스트 모임입니다.",
        "상세한 모임 설명입니다.",
        "모임 계획입니다.",
        "서울 강남구",
        5,
        10,
        "img.jpg",
        List.of("자바", "스프링"),
        "모임장",
        Role.LEADER
    );

    given(groupQueryService.detailGroup(anyLong(), any())).willReturn(mockResponse);

    // when & then
    mockMvc.perform(get("/api/v1/groups/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("테스트 모임"))
        .andExpect(jsonPath("$.data.category").value("IT/개발"))
        .andExpect(jsonPath("$.data.summary").value("테스트 모임입니다."))
        .andExpect(jsonPath("$.data.ownerName").value("모임장"));
  }

  @Test
  @DisplayName("[성공] 모임 일정 리스트 조회")
  void getGroupSchedule_Success() throws Exception {
    // given
    ScheduleListResponse.ScheduleSummary mockSchedule = new ScheduleListResponse.ScheduleSummary(
        1L,
        "스터디 모임",
        "자바 기초 학습",
        "서울 강남구",
        ScheduleStatus.RECRUITING,
        5,
        "스터디장",
        "2025-05-25 14:00",
        "2025-05-01 09:00"
    );

    ScheduleListResponse response = new ScheduleListResponse(
        List.of(mockSchedule),
        0,
        1,
        0,
        true
    );

    given(scheduleQueryService.getGroupSchedules(anyLong(), any())).willReturn(response);

    // when & then
    mockMvc.perform(get("/api/v1/groups/1/schedules")
            .param("page", "0")
            .param("size", "10")
            .param("sort", "startAt"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.schedules[0].scheduleId").value(1))
        .andExpect(jsonPath("$.data.schedules[0].title").value("스터디 모임"))
        .andExpect(jsonPath("$.data.totalElements").value(1));
  }

  // ===== 모임 생성 테스트 =====

  @Test
  @DisplayName("[성공] 모임 생성")
  void createGroup_Success() throws Exception {
    // given
    GroupCreateRequest request = new GroupCreateRequest(
        "테스트 모임",
        "테스트 모임입니다.",
        "상세한 모임 설명입니다.",
        "IT/개발",
        "서울 강남구",
        10,
        "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAAAAAAAD//2Q==",
        List.of("자바", "스프링"),
        "모임 계획입니다."
    );

    GroupCreateResponse mockResponse = new GroupCreateResponse(
        1L, "테스트 모임", "IT/개발", "테스트 모임입니다.",
        "상세한 모임 설명입니다.", "모임 계획입니다.", "서울 강남구",
        1, 10, "test.jpg", List.of("자바", "스프링")
    );

    given(groupCommandService.createGroup(any(GroupCreateRequest.class), any(CustomUserDetails.class)))
        .willReturn(mockResponse);
    doNothing().when(aiGroupService).notifyGroupCreated(mockResponse);
    doNothing().when(groupEventPublisher).publishGroupCreated(mockResponse);

    // when & then
    mockMvc.perform(post("/api/v1/groups")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.data.name").value("테스트 모임"))
        .andExpect(jsonPath("$.data.category").value("IT/개발"));

    then(groupCommandService).should().createGroup(any(GroupCreateRequest.class), any(CustomUserDetails.class));
  }

  @Test
  @DisplayName("[실패] 모임 생성 - 이름이 빈 값")
  void createGroup_EmptyName_BadRequest() throws Exception {
    // given
    GroupCreateRequest invalidRequest = new GroupCreateRequest(
        "", // 빈 이름
        "테스트 모임입니다.",
        "상세한 모임 설명입니다.",
        "IT/개발",
        "서울 강남구",
        10,
        "test.jpg",
        List.of("자바", "스프링"),
        "모임 계획입니다."
    );

    // when & then
    mockMvc.perform(post("/api/v1/groups")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[실패] 모임 생성 - 태그가 빈 리스트")
  void createGroup_EmptyTags_BadRequest() throws Exception {
    // given
    GroupCreateRequest invalidRequest = new GroupCreateRequest(
        "테스트 모임",
        "테스트 모임입니다.",
        "상세한 모임 설명입니다.",
        "IT/개발",
        "서울 강남구",
        10,
        "test.jpg",
        List.of(), // 빈 태그 리스트
        "모임 계획입니다."
    );

    // when & then
    mockMvc.perform(post("/api/v1/groups")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[실패] 모임 생성 - 최대 인원수 초과")
  void createGroup_MaxUserCountExceeded_BadRequest() throws Exception {
    // given
    GroupCreateRequest request = new GroupCreateRequest(
        "테스트 모임",
        "테스트 모임입니다.",
        "상세한 모임 설명입니다.",
        "IT/개발",
        "서울 강남구",
        150, // 최대값(100) 초과
        "test.jpg",
        List.of("자바", "스프링"),
        "모임 계획입니다."
    );

    // when & then
    mockMvc.perform(post("/api/v1/groups")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[실패] 모임 생성 - 음수 인원수")
  void createGroup_NegativeUserCount_BadRequest() throws Exception {
    // given
    GroupCreateRequest request = new GroupCreateRequest(
        "테스트 모임",
        "테스트 모임입니다.",
        "상세한 모임 설명입니다.",
        "IT/개발",
        "서울 강남구",
        -5, // 음수
        "test.jpg",
        List.of("자바", "스프링"),
        "모임 계획입니다."
    );

    // when & then
    mockMvc.perform(post("/api/v1/groups")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[실패] 모임 생성 - CSRF 토큰 없음")
  void createGroup_NoCsrfToken_Forbidden() throws Exception {
    // given
    GroupCreateRequest request = new GroupCreateRequest(
        "테스트 모임", "테스트입니다.", "테스트를 해봅니다.",
        "IT/개발", "서울 강남구", 10, "test.jpg",
        List.of("test1", "test2"), "모임 계획입니다."
    );

    // when & then
    mockMvc.perform(post("/api/v1/groups")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("[실패] 모임 생성 - 유효하지 않은 카테고리")
  void createGroup_InvalidCategory_BadRequest() throws Exception {
    // given
    GroupCreateRequest request = new GroupCreateRequest(
        "테스트 모임",
        "테스트 모임입니다.",
        "상세한 모임 설명입니다.",
        "잘못된카테고리",
        "서울 강남구",
        10,
        "test.jpg",
        List.of("자바"),
        "모임 계획입니다."
    );

    given(groupCommandService.createGroup(any(GroupCreateRequest.class), any(CustomUserDetails.class)))
        .willThrow(new GroupException(GroupErrorCode.INVALID_CATEGORY));

    // when & then
    mockMvc.perform(post("/api/v1/groups")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // ===== AI 모임 생성 테스트 =====

  @Test
  @DisplayName("[성공] AI 모임 정보 생성")
  void generateGroup_Success() throws Exception {
    // given
    GroupGenerateRequest request = new GroupGenerateRequest(
        "AI 스터디",
        "인공지능 기술 학습",
        "IT/개발",
        "서울 강남구",
        "3개월",
        8,
        true
    );

    GroupGenerateResponse mockResponse = new GroupGenerateResponse(
        "AI 스터디",
        "IT/개발",
        "인공지능 기술을 학습하는 모임입니다.",
        "머신러닝과 딥러닝을 중심으로 학습합니다.",
        "체계적인 AI 학습 계획",
        "서울 강남구",
        8,
        List.of("AI", "머신러닝", "딥러닝")
    );

    given(groupCommandService.generate(any(GroupGenerateRequest.class))).willReturn(mockResponse);

    // when & then
    mockMvc.perform(post("/api/v1/groups/ai-generate")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("AI 스터디"))
        .andExpect(jsonPath("$.data.category").value("IT/개발"));
  }

  // ===== 기타 테스트 =====

  @Test
  @DisplayName("[실패] 모임 상세 조회 - 존재하지 않는 모임")
  void getGroupDetail_NotFound() throws Exception {
    // given
    given(groupQueryService.detailGroup(anyLong(), any()))
        .willThrow(new GroupException(GroupErrorCode.GROUP_NOT_FOUND));

    // when & then
    mockMvc.perform(get("/api/v1/groups/999"))
        .andExpect(status().isNotFound());
  }
}
