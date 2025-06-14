package kr.ai.nemo.domain.group.service;

import jakarta.validation.Valid;
import java.util.List;
import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.request.GroupAiGenerateRequest;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRequest;
import kr.ai.nemo.domain.group.dto.request.GroupAiRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.GroupChatbotQuestionRequest;
import kr.ai.nemo.domain.group.dto.request.GroupCreateRequest;
import kr.ai.nemo.domain.group.dto.request.GroupGenerateRequest;
import kr.ai.nemo.domain.group.dto.request.GroupRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.UpdateGroupImageRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiGenerateResponse;
import kr.ai.nemo.domain.group.dto.response.GroupAiRecommendResponse;
import kr.ai.nemo.domain.group.dto.response.GroupAiRecommendTextResponse;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotQuestionResponse;
import kr.ai.nemo.domain.group.dto.response.GroupCreateResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDto;
import kr.ai.nemo.domain.group.dto.response.GroupGenerateResponse;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsCommandService;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.infra.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupCommandService {

  private final GroupRepository groupRepository;
  private final GroupTagService groupTagService;
  private final GroupParticipantsCommandService groupParticipantsCommandService;
  private final AiGroupService aiClient;
  private final ImageService imageService;
  private final GroupValidator groupValidator;

  @TimeTrace
  @Transactional
  public GroupGenerateResponse generate(GroupGenerateRequest request) {
    GroupAiGenerateRequest aiRequest = GroupAiGenerateRequest.from(request);
    GroupAiGenerateResponse aiResponse = aiClient.call(aiRequest);
    return GroupGenerateResponse.from(request, aiResponse);
  }

  @TimeTrace
  @Transactional
  public GroupCreateResponse createGroup(@Valid GroupCreateRequest request, CustomUserDetails userDetails) {

    groupValidator.isCategory(request.category());

    Group group = Group.builder()
        .owner(userDetails.getUser())
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

    groupParticipantsCommandService.applyToGroup(savedGroup.getId(), userDetails, Role.LEADER, Status.JOINED);

    List<String> tags = groupTagService.getTagNamesByGroupId(savedGroup.getId());

    return GroupCreateResponse.from(savedGroup, tags);
  }

  @TimeTrace
  @Transactional
  public void deleteGroup(Long groupId, Long userId) {
    Group group = groupValidator.isOwnerForGroupDelete(groupId, userId);
    group.deleteGroup();
  }

  @TimeTrace
  @Transactional
  public void updateGroupImage(Long groupId, Long userId, UpdateGroupImageRequest request) {
    Group group = groupValidator.isOwnerForGroupUpdate(groupId, userId);
    group.setImageUrl(imageService.updateImage(group.getImageUrl(), request.imageUrl()));
  }

  @TimeTrace
  @Transactional
  public GroupAiRecommendTextResponse recommendGroupFreeform(GroupRecommendRequest request, Long userId) {
    GroupAiRecommendRequest aiRequest = new GroupAiRecommendRequest(userId, request.requestText());
    GroupAiRecommendResponse aiResponse = aiClient.recommendGroupFreeform(aiRequest);
    Group group = groupValidator.findByIdOrThrow(aiResponse.groupId());
    GroupDto groupDto = GroupDto.from(group);
    return new GroupAiRecommendTextResponse(groupDto, aiResponse.responseText());
  }

  @TimeTrace
  @Transactional
  public GroupChatbotQuestionResponse recommendGroupQuestion(GroupChatbotQuestionRequest request, Long userId, String sessionId) {
    GroupAiQuestionRequest aiRequest = new GroupAiQuestionRequest(userId, request.answer());
    return aiClient.recommendGroupQuestion(aiRequest, sessionId);
  }
}
