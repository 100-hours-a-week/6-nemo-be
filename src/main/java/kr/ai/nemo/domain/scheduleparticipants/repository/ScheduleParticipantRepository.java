package kr.ai.nemo.domain.scheduleparticipants.repository;

import java.util.List;
import java.util.Optional;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.domain.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScheduleParticipantRepository extends JpaRepository<ScheduleParticipant, Long> {

  @EntityGraph(attributePaths = {"user"})
  List<ScheduleParticipant> findByScheduleId(Long scheduleId);

  boolean existsByScheduleAndUser(Schedule schedule, User user);

  Optional<ScheduleParticipant> findByScheduleIdAndUserId(Long scheduleId, Long userId);

  @Query("""
    SELECT sp FROM ScheduleParticipant sp
    LEFT JOIN FETCH sp.schedule s
    LEFT JOIN FETCH s.group g
    LEFT JOIN FETCH s.owner o
    WHERE sp.user.id = :userId
    AND s.status = :status
""")
  List<ScheduleParticipant> findRecruitingSchedulesByUserId(Long userId, ScheduleStatus status);
}
