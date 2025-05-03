package kr.ai.nemo.group.repository;

import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
  @Query("SELECT g FROM Group g WHERE g.name LIKE %:keyword% OR g.summary LIKE %:keyword%")
  Page<Group> searchWithKeywordOnly(@Param("keyword") String keyword, Pageable pageable);

  Page<Group> findByCategory(Category categoryEnum, Pageable pageable);
}