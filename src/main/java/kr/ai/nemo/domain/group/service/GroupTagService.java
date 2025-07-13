package kr.ai.nemo.domain.group.service;

import java.util.List;

import kr.ai.nemo.global.aop.logging.TimeTrace;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.GroupTag;
import kr.ai.nemo.domain.group.domain.Tag;
import kr.ai.nemo.domain.group.repository.GroupTagRepository;
import kr.ai.nemo.domain.group.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupTagService {

  private final TagRepository tagRepository;
  private final GroupTagRepository groupTagRepository;

  @TimeTrace
  @Transactional
  public void assignTags(Group group, List<String> tagNames) {
    for (String tagName : tagNames) {
      Tag tag = tagRepository.findByName(tagName)
          .orElseGet(() -> tagRepository.save(new Tag(tagName)));

      GroupTag groupTag = GroupTag.builder()
          .group(group)
          .tag(tag)
          .build();

      groupTagRepository.save(groupTag);
    }
  }

  @TimeTrace
  @Transactional(readOnly = true)
  public List<String> getTagNamesByGroupId(Long groupId) {
    return groupTagRepository.findByGroupId(groupId).stream()
        .map(groupTag -> groupTag.getTag().getName())
        .toList();
  }
}
