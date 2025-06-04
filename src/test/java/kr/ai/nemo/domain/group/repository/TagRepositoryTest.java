package kr.ai.nemo.domain.group.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.Optional;
import kr.ai.nemo.domain.group.domain.Tag;
import kr.ai.nemo.global.fixture.group.TagFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TagRepository 테스트")
class TagRepositoryTest {

  @Autowired
  private TagRepository tagRepository;

  @Test
  @DisplayName("[성공] Tag 저장 테스트")
  void save_Success() {
    // given
    Tag tag = TagFixture.createDefaultTag();

    // when
    Tag saved = tagRepository.save(tag);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getName()).isEqualTo("testTag");
  }

  @Test
  void save_ShouldFail_WhenTagNameIsNull() {
    // given
    Tag tag = Tag.builder().name(null).build();

    // when & then
    assertThatThrownBy(() -> tagRepository.saveAndFlush(tag))
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void save_ShouldFail_WhenTagNameIsEmpty() {
    // given
    Tag tag = Tag.builder().name("").build();

    // when & then
    assertThatThrownBy(() -> tagRepository.saveAndFlush(tag))
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  @DisplayName("[성공] 존재하는 태그명으로 조회")
  void findByName_Success() {
    // given
    Tag tag = TagFixture.createDefaultTag();
    Tag saved = tagRepository.save(tag);

    // when
    Optional<Tag> findTag = tagRepository.findByName(saved.getName());

    // then
    assertThat(findTag)
        .isPresent()
        .hasValueSatisfying(foundTag ->
            assertThat(foundTag.getName()).isEqualTo(saved.getName())
        );
  }

  @Test
  @DisplayName("[실패] 중복 태그명으로 저장 실패")
  void save_ShouldFail_WhenDuplicateTagName() {
    // given
    String tagName = "duplicateTag";
    Tag firstTag = Tag.builder().name(tagName).build();
    Tag duplicateTag = Tag.builder().name(tagName).build();

    // 첫 번째 태그 저장
    tagRepository.saveAndFlush(firstTag);

    // when & then - 중복 태그 저장 시도
    assertThatThrownBy(() -> tagRepository.saveAndFlush(duplicateTag))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("constraint");
  }

  @Test
  @DisplayName("[실패] 존재하지 않는 ID로 조회시 예외")
  void getReferenceById_ShouldFail_WhenIdNotExists() {
    // given
    Long nonExistentId = 9999L;
    Tag tag = tagRepository.getReferenceById(nonExistentId);

    // when & then
    assertThatThrownBy(tag::toString)
        .isInstanceOf(EntityNotFoundException.class);
    }
}
