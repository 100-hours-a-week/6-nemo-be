package kr.ai.nemo.group.service;

import jakarta.validation.Valid;
import java.util.List;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.enums.GroupStatus;
import kr.ai.nemo.group.domain.GroupTag;
import kr.ai.nemo.group.domain.Tag;
import kr.ai.nemo.group.dto.GroupCreateRequest;
import kr.ai.nemo.group.dto.GroupCreateResponse;
import kr.ai.nemo.group.repository.GroupRepository;
import kr.ai.nemo.group.repository.GroupTagRepository;
import kr.ai.nemo.group.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupCommandService {

  private final GroupRepository groupRepository;
  private final TagRepository tagRepository;
  private final GroupTagRepository groupTagRepository;

  @Transactional
  public GroupCreateResponse createGroup(@Valid GroupCreateRequest request) {

    Group group = Group.builder()
        .name(request.getName())
        .summary(request.getSummary())
        .description(request.getDescription())
        .plan(request.getPlan())
        .category(request.getCategory())
        .location(request.getLocation())
        .completedScheduleTotal(0)
        .imageUrl(request.getImageUrl())
        .currentUserCount(1)
        .maxUserCount(request.getMaxUserCount())
        .status(GroupStatus.ACTIVE)
        .build();


    Group savedGroup = groupRepository.save(group);

    if (request.getTags() != null && !request.getTags().isEmpty()) {
      processTags(savedGroup, request.getTags());
    }

    return GroupCreateResponse.from(savedGroup);
  }

  private void processTags(Group group, List<String> tagNames) {
    for (String tagName : tagNames) {

      Tag tag = tagRepository.findByName(tagName)
          .orElseGet(() -> {
            Tag newTag = new Tag(tagName);
            return tagRepository.save(newTag);
          });

      GroupTag groupTag = GroupTag.builder()
          .group(group)
          .tag(tag)
          .build();

      group.getGroupTags().add(groupTag);

      groupTagRepository.save(groupTag);
    }
  }
}