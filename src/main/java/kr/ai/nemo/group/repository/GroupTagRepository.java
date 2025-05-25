package kr.ai.nemo.group.repository;

import java.util.List;
import kr.ai.nemo.group.domain.GroupTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupTagRepository extends JpaRepository<GroupTag, Long> {

  List<GroupTag> findByGroupId(Long groupId);
}
