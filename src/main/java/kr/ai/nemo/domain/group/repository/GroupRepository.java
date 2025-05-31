package kr.ai.nemo.domain.group.repository;

import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
  @EntityGraph(attributePaths = {"groupTags", "groupTags.tag"})
  @Query("""
    SELECT DISTINCT g FROM Group g
    WHERE g.status <> 'DISBANDED' AND (
        g.name LIKE %:keyword% OR
        g.summary LIKE %:keyword%
    )
""")
  Page<Group> searchWithKeywordOnly(@Param("keyword") String keyword, Pageable pageable);

  @EntityGraph(attributePaths = {"groupTags", "groupTags.tag"})
  Page<Group> findByCategoryAndStatusNot(String category, GroupStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {"groupTags", "groupTags.tag"})
  Page<Group> findByStatusNot(GroupStatus status, Pageable pageable);

  Group getGroupById(Long id);
}
