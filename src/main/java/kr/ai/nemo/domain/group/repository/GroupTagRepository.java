package kr.ai.nemo.domain.group.repository;

import java.util.List;
import kr.ai.nemo.domain.group.domain.GroupTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupTagRepository extends JpaRepository<GroupTag, Long> {

  @EntityGraph(attributePaths = {"tag"})
  List<GroupTag> findByGroupId(Long groupId);
}
