package kr.ai.nemo.group.service;

import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.enums.GroupStatus;
import kr.ai.nemo.group.dto.GroupDetailResponse;
import kr.ai.nemo.group.dto.GroupDto;
import kr.ai.nemo.group.dto.GroupListResponse;
import kr.ai.nemo.group.dto.GroupSearchRequest;
import kr.ai.nemo.group.repository.GroupRepository;
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

  public GroupListResponse getGroups(GroupSearchRequest request) {
    Pageable pageable = toPageable(request);

    Page<Group> groups;

    if (request.getCategoryEnum() != null) {
      groups = groupRepository.findByCategory(request.getCategoryEnum(), pageable);
    } else if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
      groups = groupRepository.searchWithKeywordOnly(request.getKeyword(), pageable);
    } else {
      groups = groupRepository.findAll(pageable);
    }

    Page<GroupDto> groupDtoPage = groups.map(GroupDto::from);

    return GroupListResponse.from(groupDtoPage);
  }

  private GroupDetailResponse convertToDetailResponse(Group group) {
    return GroupDetailResponse.from(group);
  }

  public GroupDetailResponse detailGroup(Long groupId) {
    Group group = findByIdOrThrow(groupId);
    return convertToDetailResponse(group);
  }

  private Pageable toPageable(GroupSearchRequest request) {
    Sort sort = Sort.by(Sort.Direction.fromString(request.getDirection()), request.getSort());
    return PageRequest.of(request.getPage(), request.getSize(), sort);
  }

  public Group findByIdOrThrow(Long groupId) {
    Group group = groupRepository.findById(groupId)
        .orElseThrow(() -> new CustomException(ResponseCode.GROUP_NOT_FOUND));

    if (group.getStatus() == GroupStatus.DISBANDED) {
      throw new CustomException(ResponseCode.GROUP_DISBANDED);
    }
    return group;
  }
}