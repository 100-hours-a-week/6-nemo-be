package kr.ai.nemo.group.repository;

import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.enums.GroupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
  @Query("""
      SELECT DISTINCT g FROM Group g
 LEFT JOIN g.groupTags t
 WHERE g.status <> 'DISBANDED' AND (
     g.name LIKE %:keyword% OR
     g.summary LIKE %:keyword% OR
     t.tag.name LIKE %:keyword%
 )
""")
  Page<Group> searchWithKeywordOnly(@Param("keyword") String keyword, Pageable pageable);

  @EntityGraph(attributePaths = {"groupTags", "groupTags.tag"})
  Page<Group> findByCategoryAndStatusNot(String category, GroupStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {"groupTags", "groupTags.tag"})
  Page<Group> findByStatusNot(GroupStatus status, Pageable pageable);

  Group getGroupById(Long id);
}
