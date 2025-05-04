package kr.ai.nemo.group.service;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.enums.GroupStatus;
import kr.ai.nemo.group.dto.GroupCreateRequest;
import kr.ai.nemo.group.dto.GroupCreateResponse;
import kr.ai.nemo.group.participants.domain.GroupParticipants;
import kr.ai.nemo.group.participants.domain.enums.Role;
import kr.ai.nemo.group.participants.domain.enums.Status;
import kr.ai.nemo.group.participants.repository.GroupParticipantsRepository;
import kr.ai.nemo.group.repository.GroupRepository;
import kr.ai.nemo.user.domain.User;
import kr.ai.nemo.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupCommandService {

  private final GroupRepository groupRepository;
  private final GroupTagService groupTagService;
  private final UserQueryService userQueryService;
  private final GroupParticipantsRepository groupParticipantsRepository;

  @Transactional
  public GroupCreateResponse createGroup(@Valid GroupCreateRequest request, Long userId) {

    User user = userQueryService.findByIdOrThrow(userId);

    Group group = Group.builder()
        .owner(user)
        .name(request.getName())
        .summary(request.getSummary())
        .description(request.getDescription())
        .plan(request.getPlan())
        .category(request.getCategoryEnum())
        .location(request.getLocation())
        .completedScheduleTotal(0)
        .imageUrl(request.getImageUrl())
        .currentUserCount(1)
        .maxUserCount(request.getMaxUserCount())
        .status(GroupStatus.ACTIVE)
        .build();


    Group savedGroup = groupRepository.save(group);

    if (request.getTags() != null && !request.getTags().isEmpty()) {
      groupTagService.assignTags(savedGroup, request.getTags());
    }

    GroupParticipants participants = GroupParticipants.builder()
        .user(user)
        .group(group)
        .role(Role.LEADER.getDescription()) // 또는 OWNER
        .status(Status.JOINED.getDisplayName())
        .appliedAt(LocalDateTime.now())
        .joinedAt(LocalDateTime.now())
        .build();

    groupParticipantsRepository.save(participants);

    return new GroupCreateResponse(savedGroup);
  }
}