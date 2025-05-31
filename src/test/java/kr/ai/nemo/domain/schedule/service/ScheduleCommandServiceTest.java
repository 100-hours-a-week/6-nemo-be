package kr.ai.nemo.domain.schedule.service;

import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.schedule.validator.ScheduleValidator;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleCommandService 테스트")
class ScheduleCommandServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ScheduleValidator scheduleValidator;

    @InjectMocks
    private ScheduleCommandService scheduleCommandService;

    @Test
    @DisplayName("스케줄 생성 성공 테스트")
    void createSchedule_Success() {
        // given
        
        // when
        
        // then
    }
}
