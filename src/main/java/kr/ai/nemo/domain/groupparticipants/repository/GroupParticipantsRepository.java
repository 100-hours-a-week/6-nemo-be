package kr.ai.nemo.domain.groupparticipants.repository;

import java.util.List;
import java.util.Optional;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.user.domain.User;
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

  boolean existsByGroupIdAndUserIdAndStatus(Long groupId, Long userId, Status status);

  List<GroupParticipants> user(User user);

  List<GroupParticipants> group(Group group);

  Optional<GroupParticipants> findByGroupIdAndUserId(Long groupId, Long userId);
}
