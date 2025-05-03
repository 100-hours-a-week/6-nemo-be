package kr.ai.nemo.group.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import kr.ai.nemo.group.domain.Group;
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




    List<GroupDto> groupDto = groups.getContent().stream()
        .map(GroupDto::from)
        .collect(Collectors.toList());

    return GroupListResponse.from(
        groupDto,
        (int) groups.getTotalElements(),
        groups.getNumber(),
        groups.getSize()
    );
  }

  private GroupDetailResponse convertToDetailResponse(Group group) {
    return GroupDetailResponse.from(group);
  }

  public GroupDetailResponse detailGroup(Long groupId) {
    Group group = groupRepository.findById(groupId)
        .orElseThrow(() -> new CustomException(ResponseCode.GROUP_NOT_FOUND));

    return convertToDetailResponse(group);
  }

  private Pageable toPageable(GroupSearchRequest request) {
    Sort sort = Sort.by(Sort.Direction.fromString(request.getDirection()), request.getSort());
    return PageRequest.of(request.getPage(), request.getSize(), sort);
  }

}