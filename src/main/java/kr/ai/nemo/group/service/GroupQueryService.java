package kr.ai.nemo.group.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.ai.nemo.common.exception.group.GroupNotFoundException;
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
    Sort sort = Sort.by(Sort.Direction.fromString(request.getDirection()), request.getSort());
    
    Pageable pageable = PageRequest.of(
        request.getPage(),
        request.getSize(),
        sort
    );

    Page<Group> groups = groupRepository.search(
        request.getCategory(), 
        request.getKeyword(),
        pageable
    );

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

  public GroupDetailResponse detailGroup(Long groupId) {
    Group group = groupRepository.findById(groupId)
        .orElseThrow(GroupNotFoundException::new);

    return GroupDetailResponse.from(group);
  }
}