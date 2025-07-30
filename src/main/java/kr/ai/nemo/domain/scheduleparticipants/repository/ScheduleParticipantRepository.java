package kr.ai.nemo.domain.scheduleparticipants.repository;

import java.util.List;
import java.util.Optional;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleInfoProjection;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.domain.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScheduleParticipantRepository extends JpaRepository<ScheduleParticipant, Long> {

  @EntityGraph(attributePaths = {"user"})
  @Query("""
      SELECT sp
      FROM ScheduleParticipant sp
      JOIN FETCH sp.user
      WHERE sp.schedule.id = :scheduleId
      ORDER BY sp.updatedAt ASC
      """)
  List<ScheduleParticipant> findByScheduleId(Long scheduleId);

  boolean existsByScheduleAndUser(Schedule schedule, User user);

  Optional<ScheduleParticipant> findByScheduleIdAndUserId(Long scheduleId, Long userId);

  @Query("""
    SELECT new kr.ai.nemo.domain.schedule.dto.response.ScheduleInfoProjection(
        s.id,
        s.title,
        s.description,
        s.address,
        s.status,
        s.currentUserCount,
        g.id,
        g.name,
        o.nickname,
        s.startAt,
        sp.status
    )
    FROM ScheduleParticipant sp
    JOIN sp.schedule s
    JOIN s.group g
    JOIN s.owner o
    WHERE sp.user.id = :userId
      AND s.status = 'RECRUITING'
      AND EXISTS (
        SELECT 1 FROM GroupParticipants gp
        WHERE gp.group.id = g.id AND gp.user.id = :userId
      )
    ORDER BY sp.updatedAt ASC
""")
  List<ScheduleInfoProjection> findUserRecruitingSchedules(Long userId);
}
