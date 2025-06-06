package kr.ai.nemo.domain.group.service;

import java.util.List;
import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.response.GroupDetailResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDto;
import kr.ai.nemo.domain.group.dto.response.GroupListResponse;
import kr.ai.nemo.domain.group.dto.request.GroupSearchRequest;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GroupQueryService {

  private final GroupRepository groupRepository;
  private final GroupValidator groupValidator;
  private final GroupTagService groupTagService;
  private final GroupParticipantValidator groupParticipantValidator;

  @Transactional(readOnly = true)
  public GroupListResponse getGroups(GroupSearchRequest request, Pageable pageable) {
    Page<Group> groups;

    if (request.getCategory() != null) {
      groups = groupRepository.findByCategoryAndStatusNot(request.getCategory(), GroupStatus.DISBANDED, pageable);
    } else if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
      groups = groupRepository.searchWithKeywordOnly(request.getKeyword(), pageable);
    } else {
      groups = groupRepository.findByStatusNot(GroupStatus.DISBANDED, pageable);
    }

    Page<GroupDto> groupDtoPage = groups.map(GroupDto::from);

    return GroupListResponse.from(groupDtoPage);
  }

  @TimeTrace
  @Transactional(readOnly = true)
  public GroupDetailResponse detailGroup(Long groupId, CustomUserDetails customUserDetails) {
    Group group = groupValidator.findByIdOrThrow(groupId);
    List<String> tags = groupTagService.getTagNamesByGroupId(group.getId());
    Role role = groupParticipantValidator.checkUserRole(customUserDetails, group);
    return GroupDetailResponse.from(group, tags, role);
  }
}
