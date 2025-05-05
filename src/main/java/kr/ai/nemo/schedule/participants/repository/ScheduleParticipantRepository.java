package kr.ai.nemo.schedule.participants.repository;

import java.util.List;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.participants.domain.ScheduleParticipant;
import kr.ai.nemo.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleParticipantRepository extends JpaRepository<ScheduleParticipant, Long> {

  List<ScheduleParticipant> findByScheduleId(Long scheduleId);

  boolean existsByScheduleAndUser(Schedule schedule, User user);
}
