package kr.ai.nemo.domain.group.repository;

import java.util.List;
import java.util.Optional;
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
    LEFT JOIN g.groupTags gt
    LEFT JOIN gt.tag t
    WHERE g.status <> 'DISBANDED' AND (
        g.name LIKE %:keyword% OR
        g.summary LIKE %:keyword% OR
        t.name LIKE %:keyword%
    )
    ORDER BY g.updatedAt DESC
""")
  Page<Group> searchWithKeywordOnly(@Param("keyword") String keyword, Pageable pageable);

  @EntityGraph(attributePaths = {"groupTags", "groupTags.tag"})
  @Query("""
    SELECT DISTINCT g FROM Group g
    LEFT JOIN g.groupTags gt
    LEFT JOIN gt.tag t
    WHERE g.status <> 'DISBANDED' AND (
        g.category =:category
    )
    ORDER BY g.updatedAt DESC
""")
  Page<Group> findByCategoryAndStatusNot(String category, GroupStatus status, Pageable pageable);

  @Query("""
  SELECT g.id FROM Group g
  WHERE g.category = :category AND g.status <> 'DISBANDED'
  ORDER BY g.updatedAt DESC
""")
  Page<Long> findGroupIdsByCategoryAndStatusNot(@Param("category") String category,
      @Param("status") GroupStatus status,
      Pageable pageable);

  @Query("""
  SELECT g.id FROM Group g
  WHERE g.status <> 'DISBANDED'
    AND (g.name LIKE %:keyword% OR g.summary LIKE %:keyword% OR g.description LIKE %:keyword%)
  ORDER BY g.updatedAt DESC
""")
  Page<Long> searchGroupIdsWithKeywordOnly(@Param("keyword") String keyword, Pageable pageable);

  @EntityGraph(attributePaths = {"groupTags", "groupTags.tag"})
  @Query("""
    SELECT DISTINCT g FROM Group g
    LEFT JOIN g.groupTags gt
    LEFT JOIN gt.tag t
    WHERE g.status <> 'DISBANDED'
    ORDER BY g.updatedAt DESC
""")
  Page<Group> findByStatusNot(GroupStatus status, Pageable pageable);

  @Query("""
  SELECT g.id FROM Group g
  WHERE g.status <> 'DISBANDED'
  ORDER BY g.updatedAt DESC
""")
  Page<Long> findGroupIdsByStatusNot(Pageable pageable);

  @Query("""
  SELECT DISTINCT g FROM Group g
  LEFT JOIN FETCH g.groupTags gt
  LEFT JOIN FETCH gt.tag
  WHERE g.id IN :ids
  ORDER BY g.updatedAt DESC

""")
  List<Group> findGroupsWithTagsByIds(@Param("ids") List<Long> ids);

  boolean existsByIdAndOwnerId(Long groupId, Long userId);

  @Query("""
  SELECT g FROM Group g
  LEFT JOIN FETCH g.groupTags gt
  LEFT JOIN FETCH gt.tag t
  WHERE g.id = :groupId
""")
  Optional<Group> findByIdGroupId(Long groupId);
}
