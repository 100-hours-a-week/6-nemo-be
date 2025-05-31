package kr.ai.nemo.domain.schedule.service;

import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleQueryService 테스트")
class ScheduleQueryServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ScheduleQueryService scheduleQueryService;

    @Test
    @DisplayName("내 스케줄 조회 테스트")
    void getMySchedules_Success() {
        // given
        
        // when
        
        // then
    }
}
