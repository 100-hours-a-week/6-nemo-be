package kr.ai.nemo.group.validator;

import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.enums.CategoryConstants;
import kr.ai.nemo.group.domain.enums.GroupStatus;
import kr.ai.nemo.group.exception.GroupErrorCode;
import kr.ai.nemo.group.exception.GroupException;
import kr.ai.nemo.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GroupValidator {

  private final GroupRepository repository;

  public Group findByIdOrThrow(Long groupId) {
    Group group = repository.findById(groupId)
        .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));

    if (group.getStatus() == GroupStatus.DISBANDED) {
      throw new GroupException(GroupErrorCode.GROUP_DISBANDED);
    }
    return group;
  }

  public void isCategory(String category){
    if (!CategoryConstants.VALID_CATEGORIES.contains(category)) {
      throw new GroupException(GroupErrorCode.INVALID_CATEGORY);
    }
  }

  public void validateGroupIsNotFull(Group group) {
    if (group.getMaxUserCount() <= group.getCurrentUserCount()) {
      throw new GroupException(GroupErrorCode.GROUP_FULL);
    }
  }
}
