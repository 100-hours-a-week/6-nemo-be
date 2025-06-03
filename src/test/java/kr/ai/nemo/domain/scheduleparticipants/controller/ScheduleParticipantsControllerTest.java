package kr.ai.nemo.domain.scheduleparticipants.controller;

import kr.ai.nemo.domain.scheduleparticipants.service.ScheduleParticipantsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScheduleParticipantsController.class)
@ActiveProfiles("test")
@DisplayName("ScheduleParticipantsController 테스트")
class ScheduleParticipantsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleParticipantsService scheduleParticipantsService;

    @Test
    @DisplayName("스케줄 참가 결정 API 테스트")
    void decideParticipation_Success() {
        // given
        
        // when
        
        // then
    }
}
