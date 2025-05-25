package kr.ai.nemo.group.service;

import java.util.List;

import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.GroupTag;
import kr.ai.nemo.group.domain.Tag;
import kr.ai.nemo.group.repository.GroupTagRepository;
import kr.ai.nemo.group.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupTagService {

  private final TagRepository tagRepository;
  private final GroupTagRepository groupTagRepository;

  @Transactional
  public void assignTags(Group group, List<String> tagNames) {
    for (String tagName : tagNames) {
      Tag tag = tagRepository.findByName(tagName)
          .orElseGet(() -> tagRepository.save(new Tag(tagName)));

      GroupTag groupTag = GroupTag.builder()
          .group(group)
          .tag(tag)
          .build();

      group.getGroupTags().add(groupTag);
      groupTagRepository.save(groupTag);
    }
  }

  @Transactional(readOnly = true)
  public List<String> getTagNamesByGroupId(Long groupId) {
    return groupTagRepository.findByGroupId(groupId).stream()
        .map(groupTag -> groupTag.getTag().getName())
        .toList();
  }
}
