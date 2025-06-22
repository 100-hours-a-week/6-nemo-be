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
  List<ScheduleParticipant> findByScheduleId(Long scheduleId);

  boolean existsByScheduleAndUser(Schedule schedule, User user);

  Optional<ScheduleParticipant> findByScheduleIdAndUserId(Long scheduleId, Long userId);

  @Query("""
            SELECT new kr.ai.nemo.domain.schedule.dto.response.ScheduleInfoProjection(
        sp.schedule.id,
        sp.schedule.title,
        sp.schedule.description,
        sp.schedule.address,
        sp.schedule.status,
        sp.schedule.currentUserCount,
        sp.schedule.group.id,
        sp.schedule.group.name,
        sp.schedule.owner.nickname,
        sp.schedule.startAt,
        sp.status
      )
      FROM ScheduleParticipant sp
      JOIN sp.schedule s
      JOIN s.group g
      JOIN s.owner o
      WHERE sp.user.id = :userId
        AND sp.schedule.status = 'RECRUITING'
      ORDER BY sp.updatedAt ASC
      """)
  List<ScheduleInfoProjection> findUserRecruitingSchedules(Long userId);
}
