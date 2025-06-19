package kr.ai.nemo.domain.schedule.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import kr.ai.nemo.aop.role.annotation.RequireScheduleOwner;
import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.dto.request.ScheduleCreateRequest;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleCreateResponse;
import kr.ai.nemo.domain.schedule.service.ScheduleCommandService;
import kr.ai.nemo.domain.schedule.service.ScheduleQueryService;
import kr.ai.nemo.domain.user.domain.User;
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
    Group group = mock(Group.class);
    User user = mock(User.class);

    Schedule schedule = ScheduleFixture.createDefaultSchedule(group, user,
        ScheduleStatus.RECRUITING);

    ScheduleCreateRequest request = new ScheduleCreateRequest(group.getId(), schedule.getTitle(),
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
  @DisplayName("[성공] 일정 취소(삭제) 테스트")
  void deleteSchedule_Success() throws Exception {
    // given
    Group group = mock(Group.class);
    User user = mock(User.class);

    Long ScheduleId = 1L;

    Schedule schedule = ScheduleFixture.createDefaultSchedule(group, user,
        ScheduleStatus.RECRUITING);

    // when
    doNothing().when(scheduleCommandService).deleteSchedule(any());

    // then
    mockMvc.perform(delete("/api/v1/schedules/" + ScheduleId))
        .andExpect(status().isNoContent());
  }
}
