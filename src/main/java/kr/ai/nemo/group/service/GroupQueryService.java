package kr.ai.nemo.group.service;

import java.util.List;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.enums.GroupStatus;
import kr.ai.nemo.group.dto.response.GroupDetailResponse;
import kr.ai.nemo.group.dto.response.GroupDto;
import kr.ai.nemo.group.dto.response.GroupListResponse;
import kr.ai.nemo.group.dto.request.GroupSearchRequest;
import kr.ai.nemo.group.repository.GroupRepository;
import kr.ai.nemo.group.validator.GroupValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

  public GroupListResponse getGroups(GroupSearchRequest request) {
    Pageable pageable = toPageable(request);

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

  public GroupDetailResponse detailGroup(Long groupId) {
    Group group = groupValidator.findByIdOrThrow(groupId);
    List<String> tags = groupTagService.getTagNamesByGroupId(group.getId());
    return GroupDetailResponse.from(group, tags);
  }

  private Pageable toPageable(GroupSearchRequest request) {
    Sort sort = Sort.by(Sort.Direction.fromString(request.getDirection()), request.getSort());
    return PageRequest.of(request.getPage(), request.getSize(), sort);
  }
}
