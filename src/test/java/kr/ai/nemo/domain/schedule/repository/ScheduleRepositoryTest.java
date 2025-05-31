package kr.ai.nemo.domain.schedule.repository;

import kr.ai.nemo.domain.schedule.domain.Schedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DisplayName("ScheduleRepository 테스트")
class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    @DisplayName("스케줄 저장 테스트")
    void save_Success() {
        // given
        
        // when
        
        // then
    }
}
