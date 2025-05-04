package kr.ai.nemo.group.participants.repository;

import java.util.List;
import kr.ai.nemo.group.participants.domain.GroupParticipants;
import kr.ai.nemo.group.participants.domain.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupParticipantsRepository extends JpaRepository<GroupParticipants, Long> {
  boolean existsByGroupIdAndUserIdAndStatusIn(Long groupId, Long userId, List<Status> pending);
}
