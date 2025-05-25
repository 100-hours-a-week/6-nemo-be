package kr.ai.nemo.domain.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  Page<Schedule> findByGroupIdAndStatusNot(Long groupId, PageRequest pageRequest, ScheduleStatus scheduleStatus);

  List<Schedule> findByGroupAndStatus(Group group, ScheduleStatus scheduleStatus);

  List<Schedule> findByStartAtBeforeAndStatus(LocalDateTime now, ScheduleStatus scheduleStatus);

  Optional<Schedule> findByIdAndStatusNot(Long scheduleId, ScheduleStatus scheduleStatus);

  // Group과 Owner 정보를 함께 가져오는 메서드 추가
  @Query("SELECT s FROM Schedule s JOIN FETCH s.group JOIN FETCH s.owner WHERE s.id = :scheduleId")
  Optional<Schedule> findByIdWithGroupAndOwner(@Param("scheduleId") Long scheduleId);
}
