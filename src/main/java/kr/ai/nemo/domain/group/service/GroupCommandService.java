package kr.ai.nemo.domain.group.service;

import jakarta.validation.Valid;
import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.request.GroupCreateRequest;
import kr.ai.nemo.domain.group.dto.response.GroupCreateResponse;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsCommandService;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.infra.ImageService;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupCommandService {

  private final GroupRepository groupRepository;
  private final GroupTagService groupTagService;
  private final GroupParticipantsCommandService groupParticipantsCommandService;
  private final ImageService imageService;
  private final GroupValidator groupValidator;
  private final UserValidator userValidator;

  @Transactional
  public GroupCreateResponse createGroup(@Valid GroupCreateRequest request, Long userId) {
    User user = userValidator.findByIdOrThrow(userId);

    groupValidator.isCategory(request.category());

    Group group = Group.builder()
        .owner(user)
        .name(request.name())
        .summary(request.summary())
        .description(request.description())
        .plan(request.plan())
        .category(request.category())
        .location(request.location())
        .completedScheduleTotal(0)
        .imageUrl(imageService.uploadGroupImage(request.imageUrl()))
        .currentUserCount(0)
        .maxUserCount(request.maxUserCount())
        .status(GroupStatus.ACTIVE)
        .build();

    Group savedGroup = groupRepository.save(group);

    if (request.tags() != null && !request.tags().isEmpty()) {
      groupTagService.assignTags(savedGroup, request.tags());
    }

    groupParticipantsCommandService.applyToGroup(savedGroup.getId(), user.getId(), Role.LEADER, Status.JOINED);

    List<String> tags = groupTagService.getTagNamesByGroupId(savedGroup.getId());

    return GroupCreateResponse.from(savedGroup, tags);
  }
}
