package kr.ai.nemo.group.repository;

import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

  Page<Group> findByCategory(Category category, Pageable pageable);

  Page<Group> findAll(Pageable pageable);
}