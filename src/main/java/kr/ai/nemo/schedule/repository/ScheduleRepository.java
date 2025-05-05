package kr.ai.nemo.schedule.repository;

import kr.ai.nemo.schedule.domain.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  Page<Schedule> findByGroupId(Long groupId, PageRequest pageRequest);
}
