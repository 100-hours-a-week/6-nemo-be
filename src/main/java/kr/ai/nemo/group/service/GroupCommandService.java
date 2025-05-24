package kr.ai.nemo.group.service;

import jakarta.validation.Valid;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.enums.GroupStatus;
import kr.ai.nemo.group.dto.GroupCreateRequest;
import kr.ai.nemo.group.dto.GroupCreateResponse;
import kr.ai.nemo.group.validator.GroupValidator;
import kr.ai.nemo.groupparticipants.domain.enums.Role;
import kr.ai.nemo.groupparticipants.domain.enums.Status;
import kr.ai.nemo.groupparticipants.service.GroupParticipantsService;
import kr.ai.nemo.group.repository.GroupRepository;
import kr.ai.nemo.infra.ImageService;
import kr.ai.nemo.user.domain.User;
import kr.ai.nemo.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupCommandService {

  private final GroupRepository groupRepository;
  private final GroupTagService groupTagService;
  private final GroupParticipantsService groupParticipantsService;
  private final ImageService imageService;
  private final GroupValidator groupValidator;
  private final UserValidator userValidator;

  @Transactional
  public GroupCreateResponse createGroup(@Valid GroupCreateRequest request, Long userId) {
    User user = userValidator.findByIdOrThrow(userId);

    groupValidator.isCategory(request.getCategory());

    Group group = Group.builder()
        .owner(user)
        .name(request.getName())
        .summary(request.getSummary())
        .description(request.getDescription())
        .plan(request.getPlan())
        .category(request.getCategory())
        .location(request.getLocation())
        .completedScheduleTotal(0)
        .imageUrl(imageService.uploadGroupImage(request.getImageUrl()))
        .currentUserCount(0)
        .maxUserCount(request.getMaxUserCount())
        .status(GroupStatus.ACTIVE)
        .build();

    Group savedGroup = groupRepository.save(group);

    if (request.getTags() != null && !request.getTags().isEmpty()) {
      groupTagService.assignTags(savedGroup, request.getTags());
    }

    groupParticipantsService.applyToGroup(savedGroup.getId(), user.getId(), Role.LEADER, Status.JOINED);

    return new GroupCreateResponse(savedGroup);
  }
}
