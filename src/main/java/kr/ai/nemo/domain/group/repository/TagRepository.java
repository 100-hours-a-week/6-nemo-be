package kr.ai.nemo.domain.group.repository;

import java.util.Optional;
import kr.ai.nemo.domain.group.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

  Optional<Tag> findByName(String tagName);
}
