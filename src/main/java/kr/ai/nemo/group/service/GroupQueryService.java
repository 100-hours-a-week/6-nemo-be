package kr.ai.nemo.group.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import kr.ai.nemo.common.exception.group.GroupNotFoundException;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.dto.GroupDetailResponse;
import kr.ai.nemo.group.dto.GroupDto;
import kr.ai.nemo.group.dto.GroupListResponse;
import kr.ai.nemo.group.dto.GroupSearchRequest;
import kr.ai.nemo.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class GroupQueryService {

  private final GroupRepository groupRepository;

  public GroupListResponse getGroups(GroupSearchRequest request) {
    Pageable pageable = createPageable(request);

    Page<Group> groupPage = (request.getCategory() != null)
        ? groupRepository.findByCategory(request.getCategory(), pageable)
        : groupRepository.findAll(pageable);

    List<GroupDto> groupDto = groupPage.map(GroupDto::from).getContent();

    return GroupListResponse.from(
        groupDto,
        (int) groupPage.getTotalElements(),
        groupPage.getNumber(),
        groupPage.getSize()
    );
  }

  public GroupDetailResponse detailGroup(Long groupId) {
    Group group = groupRepository.findById(groupId)
        .orElseThrow(GroupNotFoundException::new);
    return GroupDetailResponse.from(group);
  }

  private Pageable createPageable(GroupSearchRequest request) {
    return PageRequest.of(
        request.getPage(),
        request.getSize(),
        Sort.by(request.getSort())
    );
  }
}
