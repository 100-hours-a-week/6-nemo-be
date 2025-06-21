package kr.ai.nemo.domain.schedule.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import kr.ai.nemo.aop.role.annotation.RequireScheduleOwner;
import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.dto.request.ScheduleCreateRequest;
import kr.ai.nemo.domain.schedule.dto.response.MySchedulesResponse;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleCreateResponse;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleDetailResponse;
import kr.ai.nemo.domain.schedule.exception.ScheduleErrorCode;
import kr.ai.nemo.domain.schedule.exception.ScheduleException;
import kr.ai.nemo.domain.schedule.service.ScheduleCommandService;
import kr.ai.nemo.domain.schedule.service.ScheduleQueryService;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.schedule.ScheduleFixture;
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

@WebMvcTest(ScheduleController.class)
@MockMember
@Import({JwtProvider.class})
@ActiveProfiles("test")
@DisplayName("ScheduleController 테스트")
class ScheduleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ScheduleCommandService scheduleCommandService;

  @MockitoBean
  private ScheduleQueryService scheduleQueryService;

  @MockitoBean
  private CustomUserDetailsService customUserDetailsService;

  @MockitoBean
  private RequireScheduleOwner requireScheduleOwner;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("[성공] 일정 생성 테스트")
  void createSchedule_Success() throws Exception {
    // given
    User user = mock(User.class);
    Group group = GroupFixture.createDefaultGroup(user);
    Long groupId = 1L;

    Schedule schedule = ScheduleFixture.createDefaultSchedule(user, group);

    ScheduleCreateRequest request = new ScheduleCreateRequest(groupId, schedule.getTitle(),
        schedule.getDescription()
        , group.getLocation(), group.getLocation(), LocalDateTime.now());
    ScheduleCreateResponse response = ScheduleCreateResponse.from(schedule);

    // when
    given(scheduleCommandService.createSchedule(any(), any()))
        .willReturn(response);

    // then
    mockMvc.perform(post("/api/v1/schedules")
            .with(csrf())
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("[실패] 일정 생성 - 잘못된 요청 데이터")
  void createSchedule_InvalidRequest() throws Exception {
    // given - validation이 실패하도록 null과 빈 값으로 설정
    String invalidRequestJson = """
        {
          "groupId": null,
          "title": "",
          "description": "",
          "address": "",
          "addressDetail": "",
          "startAt": null
        }
        """;

    // when & then
    mockMvc.perform(post("/api/v1/schedules")
            .with(csrf())
            .content(invalidRequestJson)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[실패] 일정 생성 - 존재하지 않는 그룹")
  void createSchedule_GroupNotFound() throws Exception {
    // given
    ScheduleCreateRequest request = new ScheduleCreateRequest(999L, "테스트 일정", "설명", "위치", "상세위치", LocalDateTime.now());
    
    given(scheduleCommandService.createSchedule(any(), any()))
        .willThrow(new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

    // when & then
    mockMvc.perform(post("/api/v1/schedules")
            .with(csrf())
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("[성공] 일정 취소(삭제) 테스트")
  void deleteSchedule_Success() throws Exception {
    // given
    Long ScheduleId = 1L;

    // when
    doNothing().when(scheduleCommandService).deleteSchedule(ScheduleId);

    // then
    mockMvc.perform(delete("/api/v1/schedules/" + ScheduleId)
            .with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("[실패] 일정 삭제 - 존재하지 않는 일정")
  void deleteSchedule_NotFound() throws Exception {
    // given
    Long nonExistentScheduleId = 999L;
    
    doThrow(new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND))
        .when(scheduleCommandService).deleteSchedule(nonExistentScheduleId);

    // when & then
    mockMvc.perform(delete("/api/v1/schedules/" + nonExistentScheduleId)
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("[성공] 내 일정 목록 조회 테스트")
  void getMySchedules_Success() throws Exception {
    // given
    MySchedulesResponse response = mock(MySchedulesResponse.class);
    
    given(scheduleQueryService.getMySchedules(anyLong()))
        .willReturn(response);

    // when & then
    mockMvc.perform(get("/api/v1/schedules/me"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("[성공] 일정 상세 조회 테스트")
  void getScheduleDetail_Success() throws Exception {
    // given
    Long scheduleId = 1L;
    ScheduleDetailResponse response = mock(ScheduleDetailResponse.class);
    
    given(scheduleQueryService.getScheduleDetail(eq(scheduleId)))
        .willReturn(response);

    // when & then
    mockMvc.perform(get("/api/v1/schedules/{scheduleId}", scheduleId))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("[실패] 일정 상세 조회 - 존재하지 않는 일정")
  void getScheduleDetail_NotFound() throws Exception {
    // given
    Long nonExistentScheduleId = 999L;
    
    given(scheduleQueryService.getScheduleDetail(eq(nonExistentScheduleId)))
        .willThrow(new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

    // when & then
    mockMvc.perform(get("/api/v1/schedules/{scheduleId}", nonExistentScheduleId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("[실패] 일정 생성 - CSRF 토큰 없음")
  void createSchedule_NoCsrfToken() throws Exception {
    // given
    ScheduleCreateRequest request = new ScheduleCreateRequest(1L, "테스트 일정", "설명", "위치", "상세위치", LocalDateTime.now());

    // when & then
    mockMvc.perform(post("/api/v1/schedules")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("[실패] 일정 삭제 - CSRF 토큰 없음")
  void deleteSchedule_NoCsrfToken() throws Exception {
    // given
    Long scheduleId = 1L;

    // when & then
    mockMvc.perform(delete("/api/v1/schedules/" + scheduleId))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("[실패] 잘못된 HTTP 메서드")
  void wrongHttpMethod() throws Exception {
    // when & then
    mockMvc.perform(post("/api/v1/schedules/my")
            .with(csrf()))
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  @DisplayName("[실패] 잘못된 파라미터 타입")
  void invalidParameterType() throws Exception {
    // when & then
    mockMvc.perform(get("/api/v1/schedules/{scheduleId}", "invalid"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[실패] Content-Type 누락")
  void missingContentType() throws Exception {
    // given
    ScheduleCreateRequest request = new ScheduleCreateRequest(1L, "테스트 일정", "설명", "위치", "상세위치", LocalDateTime.now());

    // when & then
    mockMvc.perform(post("/api/v1/schedules")
            .with(csrf())
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnsupportedMediaType());
  }
}
