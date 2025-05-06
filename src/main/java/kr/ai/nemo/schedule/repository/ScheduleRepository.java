package kr.ai.nemo.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.domain.enums.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  Page<Schedule> findByGroupId(Long groupId, PageRequest pageRequest);

  List<Schedule> findByGroupAndStatus(Group group, ScheduleStatus scheduleStatus);

  List<Schedule> findByStartAtBeforeAndStatus(LocalDateTime now, ScheduleStatus scheduleStatus);
}
