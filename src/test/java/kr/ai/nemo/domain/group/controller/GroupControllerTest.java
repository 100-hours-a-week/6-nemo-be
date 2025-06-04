package kr.ai.nemo.domain.group.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import kr.ai.nemo.domain.group.service.GroupCommandService;
import kr.ai.nemo.domain.group.service.GroupGenerateService;
import kr.ai.nemo.domain.group.service.GroupQueryService;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleListResponse;
import kr.ai.nemo.domain.schedule.service.ScheduleQueryService;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.testUtil.MockMember;
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
@DisplayName("GroupController 테스트")
class GroupControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private GroupGenerateService groupGenerateService;

  @MockitoBean
  private GroupCommandService groupCommandService;

  @MockitoBean
  private GroupQueryService groupQueryService;

  @MockitoBean
  private ScheduleQueryService scheduleQueryService;

  @MockitoBean
  private CustomUserDetailsService customUserDetailsService;

  @Test
  @DisplayName("모임 list 조회 API 테스트")
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

    given(groupQueryService.getGroups(any(), any()))
        .willReturn(GroupListResponse.from(mockPage));

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
  @DisplayName("검색한 모임 list 조회 테스트")
  void getGroupsByCategory_Success() throws Exception {
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

    given(groupQueryService.getGroups(any(), any()))
        .willReturn(GroupListResponse.from(mockPage));

    // when & then
    mockMvc.perform(get("/api/v1/groups/search")
            .param("category", "IT/개발")
            .param("sort", "createdAt")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groups[0].groupId").value(1))
        .andExpect(jsonPath("$.data.groups[0].name").value("자바 스터디"))
        .andExpect(jsonPath("$.data.groups[0].tags[0]").value("자바"))
        .andExpect(jsonPath("$.data.totalPages").value(1))
        .andExpect(jsonPath("$.data.totalElements").value(1))
        .andExpect(jsonPath("$.data.pageNumber").value(0))
        .andExpect(jsonPath("$.data.isLast").value(true));
  }

  @Test
  @DisplayName("검색한 모임 list 조회 테스트")
  void getGroupsByKeyword_Success() throws Exception {
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

    given(groupQueryService.getGroups(any(), any()))
        .willReturn(GroupListResponse.from(mockPage));

    // when & then
    mockMvc.perform(get("/api/v1/groups/search")
            .param("keyword", "자바")
            .param("sort", "createdAt")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groups[0].groupId").value(1))
        .andExpect(jsonPath("$.data.groups[0].name").value("자바 스터디"))
        .andExpect(jsonPath("$.data.groups[0].tags[0]").value("자바"))
        .andExpect(jsonPath("$.data.totalPages").value(1))
        .andExpect(jsonPath("$.data.totalElements").value(1))
        .andExpect(jsonPath("$.data.pageNumber").value(0))
        .andExpect(jsonPath("$.data.isLast").value(true));
  }

  @Test
  @DisplayName("모임 생성 API 테스트")
  void createGroup_Success() throws Exception {
    // given
    GroupCreateRequest request = new GroupCreateRequest(
        "테스트 모임", "테스트입니다.", "테스트를 해봅니다.",
        "IT/개발", "서울 강남구", 10, "test.jpg",
        List.of("test1", "test2"), "모임 계획입니다."
    );

    GroupCreateResponse mockResponse = new GroupCreateResponse(
        1L, "테스트 모임", "IT/개발", "테스트입니다.",
        "테스트를 해봅니다.", "모임 계획입니다.", "서울 강남구",
        1, 10, "test.jpg", List.of("test1", "test2")
    );

    given(groupCommandService.createGroup(any(GroupCreateRequest.class), any(CustomUserDetails.class)))
        .willReturn(mockResponse);

    // when & then
    mockMvc.perform(post("/api/v1/groups")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.name").value("테스트 모임"))
        .andExpect(jsonPath("$.data.category").value("IT/개발"));

    // verify
    then(groupCommandService).should().createGroup(
        any(GroupCreateRequest.class),
        any(CustomUserDetails.class)
    );
  }

  @Test
  @DisplayName("모임 상세 조회 테스트")
  void getGroupDetail_Success() throws Exception {
    GroupDetailResponse mockResponse = new GroupDetailResponse(
        "test",
        "IT/개발",
        "test입니다.",
        "test입니다!!",
        "test할 예정입니다.",
        "서울 강남구",
        10,
        15,
        "img.jpg",
        List.of("test1", "test2"),
        "test1",
        Role.LEADER
    );

    given(groupQueryService.detailGroup(anyLong(), any())).willReturn(mockResponse);

    mockMvc.perform(get("/api/v1/groups/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("test"))
        .andExpect(jsonPath("$.data.category").value("IT/개발"))
        .andExpect(jsonPath("$.data.summary").value("test입니다."))
        .andExpect(jsonPath("$.data.ownerName").value("test1"));
  }

  @Test
  @DisplayName("모임 일정 list 조회")
  void getGroupSchedule_Success() throws Exception {
    ScheduleListResponse.ScheduleSummary mockResponse = new ScheduleListResponse.ScheduleSummary(
          1L,
        "test1",
        "test입니다.",
        "서울 강남구",
        ScheduleStatus.RECRUITING,
        1,
        "test",
        "2025-05-25 14:00",
        "2025-05-01 09:00"
    );

    ScheduleListResponse response = new ScheduleListResponse(
        List.of(mockResponse),
        0,
        1,
        0,
        true
    );

    // when
    given(scheduleQueryService.getGroupSchedules(anyLong(), any()))
        .willReturn(response);

    mockMvc.perform(get("/api/v1/groups/1/schedules")
            .param("page", "0")
            .param("size", "10")
            .param("sort", "createdAt"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.schedules[0].scheduleId").value(1))
        .andExpect(jsonPath("$.data.schedules[0].title").value("test1"))
        .andExpect(jsonPath("$.data.schedules[0].address").value("서울 강남구"))
        .andExpect(jsonPath("$.data.totalElements").value(1))
        .andExpect(jsonPath("$.data.pageNumber").value(0))
        .andExpect(jsonPath("$.data.isLast").value(true));
  }

  @Test
  @DisplayName("AI에 모임 정보 생성 요청 테스트")
  void generateGroup_Success() throws Exception {
    GroupGenerateRequest request = new GroupGenerateRequest(
        "test",
        "test하기",
        "IT/개발",
        "서울 강남구",
        "1개월 이하",
        10,
        true
    );

    GroupGenerateResponse mockResponse = new GroupGenerateResponse(
        "test",
        "IT/개발",
        "test입니다.",
        "test입니다!!",
        "test할 예정입니다.",
        "서울 강남구",
        10,
        List.of("test1", "test2")
    );

    given(groupGenerateService.generate(any(GroupGenerateRequest.class)))
        .willReturn(mockResponse);

    mockMvc.perform(post("/api/v1/groups/ai-generate")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("test"));
  }
}
