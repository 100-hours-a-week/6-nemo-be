package kr.ai.nemo.domain.scheduleparticipants.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.domain.scheduleparticipants.dto.ScheduleParticipantDecisionRequest;
import kr.ai.nemo.domain.scheduleparticipants.service.ScheduleParticipantsService;
import kr.ai.nemo.unit.global.testUtil.MockMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScheduleParticipantsController.class)
@MockMember
@Import({JwtProvider.class})
@ActiveProfiles("test")
@DisplayName("ScheduleParticipantsController 테스트")
class ScheduleParticipantsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleParticipantsService scheduleParticipantsService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("[성공] 스케줄 참가 수락")
    void decideParticipation_Accept_Success() throws Exception {
        // given
        Long scheduleId = 1L;

        ScheduleParticipantDecisionRequest request = new ScheduleParticipantDecisionRequest(
            ScheduleParticipantStatus.ACCEPTED);

        doNothing().when(scheduleParticipantsService)
                .decideParticipation(anyLong(), anyLong(), any());

        // when & then
        mockMvc.perform(patch("/api/v1/schedules/{scheduleId}/participants", scheduleId)  // v1으로 수정
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());  // 200 OK (ResponseEntity.ok() 반환)
    }

    @Test
    @DisplayName("[성공] 스케줄 참가 거절")
    void decideParticipation_Reject_Success() throws Exception {
        // given
        Long scheduleId = 1L;
        ScheduleParticipantDecisionRequest request = new ScheduleParticipantDecisionRequest(ScheduleParticipantStatus.REJECTED);

        doNothing().when(scheduleParticipantsService)
                .decideParticipation(anyLong(), anyLong(), any());

        // when & then
        mockMvc.perform(patch("/api/v1/schedules/{scheduleId}/participants", scheduleId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[실패] 스케줄 참가 결정 - 잘못된 상태값")
    void decideParticipation_InvalidStatus_BadRequest() throws Exception {
        // given
        Long scheduleId = 1L;
        String jsonContent = "{\"status\":\"INVALID_STATUS\"}";

        // when & then
        mockMvc.perform(patch("/api/v1/schedules/{scheduleId}/participants", scheduleId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[실패] 스케줄 참가 결정 - null 상태값")
    void decideParticipation_NullStatus_BadRequest() throws Exception {
        // given
        Long scheduleId = 1L;
        String requestJson = """
        {
            "status": "INVALID_STATUS_VALUE"
        }
        """;
        // when & then
        mockMvc.perform(patch("/api/v1/schedules/{scheduleId}/participants", scheduleId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestJson)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[실패] 스케줄 참가 결정 - CSRF 토큰 없음")
    void decideParticipation_NoCsrfToken_Forbidden() throws Exception {
        // given
        Long scheduleId = 1L;
        ScheduleParticipantDecisionRequest request = new ScheduleParticipantDecisionRequest(
            ScheduleParticipantStatus.ACCEPTED);

        // when & then
        mockMvc.perform(patch("/api/v1/schedules/{scheduleId}/participants", scheduleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[실패] 스케줄 참가 결정 - 잘못된 Content-Type")
    void decideParticipation_InvalidContentType_BadRequest() throws Exception {
        // given
        Long scheduleId = 1L;
        ScheduleParticipantDecisionRequest request = new ScheduleParticipantDecisionRequest(
            ScheduleParticipantStatus.ACCEPTED);

        // when & then
        mockMvc.perform(patch("/api/v1/schedules/{scheduleId}/participants", scheduleId)
                .with(csrf())
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("[실패] 스케줄 참가 결정 - 잘못된 HTTP 메서드")
    void decideParticipation_WrongMethod_MethodNotAllowed() throws Exception {
        // given
        Long scheduleId = 1L;
        ScheduleParticipantDecisionRequest request = new ScheduleParticipantDecisionRequest(
            ScheduleParticipantStatus.ACCEPTED);

        // when & then
        mockMvc.perform(post("/api/v1/schedules/{scheduleId}/participants", scheduleId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("[실패] 스케줄 참가 결정 - 빈 문자열")
    void decideParticipation_EmptyStatus_BadRequest() throws Exception {
        // given
        Long scheduleId = 1L;
        String jsonContent = "{\"status\":\"\"}";

        // when & then
        mockMvc.perform(patch("/api/v1/schedules/{scheduleId}/participants", scheduleId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest());
    }
}
