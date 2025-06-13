package kr.ai.nemo.domain.scheduleparticipants.service;

import kr.ai.nemo.domain.scheduleparticipants.repository.ScheduleParticipantRepository;
import kr.ai.nemo.domain.scheduleparticipants.validator.ScheduleParticipantValidator;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleParticipantsService 테스트")
class ScheduleParticipantsServiceTest {

    @Mock
    private ScheduleParticipantRepository scheduleParticipantRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScheduleParticipantValidator scheduleParticipantValidator;

    @InjectMocks
    private ScheduleParticipantsService scheduleParticipantsService;

    @Test
    @DisplayName("스케줄 참가 결정 성공 테스트")
    void decideParticipation_Success() {
        // given
        
        // when
        
        // then
    }
}
