package kr.ai.nemo.domain.schedule.controller;

import kr.ai.nemo.domain.schedule.service.ScheduleCommandService;
import kr.ai.nemo.domain.schedule.service.ScheduleQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScheduleController.class)
@DisplayName("ScheduleController 테스트")
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleCommandService scheduleCommandService;

    @MockBean
    private ScheduleQueryService scheduleQueryService;

    @Test
    @DisplayName("스케줄 생성 API 테스트")
    void createSchedule_Success() {
        // given
        
        // when
        
        // then
    }
}
