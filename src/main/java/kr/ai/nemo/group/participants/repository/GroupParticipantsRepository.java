package kr.ai.nemo.group.participants.repository;

import java.util.List;
import kr.ai.nemo.group.participants.domain.GroupParticipants;
import kr.ai.nemo.group.participants.domain.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupParticipantsRepository extends JpaRepository<GroupParticipants, Long> {
  boolean existsByGroupIdAndUserIdAndStatusIn(Long groupId, Long userId, List<Status> pending);

  @Query("SELECT gp FROM GroupParticipants gp JOIN FETCH gp.user WHERE gp.group.id = :groupId AND gp.status = :status")
  List<GroupParticipants> findByGroupIdAndStatus(Long groupId, Status status);

  @Query("""
  SELECT gp FROM GroupParticipants gp
  JOIN FETCH gp.group g
  LEFT JOIN FETCH g.groupTags gt
  LEFT JOIN FETCH gt.tag
  WHERE gp.user.id = :userId
    AND gp.status = :status
""")
  List<GroupParticipants> findByUserIdAndStatus(Long userId, Status status);
}
