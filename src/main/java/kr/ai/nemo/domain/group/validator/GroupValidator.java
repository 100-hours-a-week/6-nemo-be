package kr.ai.nemo.domain.group.validator;

import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.CategoryConstants;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.repository.GroupRepository;
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

  public Group isOwner(Long groupId, Long userId) {
    Group group = findByIdOrThrow(groupId);
    if(!group.getOwner().getId().equals(userId)) {
      throw new GroupException(GroupErrorCode.GROUP_KICK_FORBIDDEN);
    }
    return group;
  }

  public Group isOwnerForGroupDelete(Long groupId, Long userId) {
    Group group = findByIdOrThrow(groupId);
    if(!group.getOwner().getId().equals(userId)) {
      throw new GroupException(GroupErrorCode.GROUP_DELETE_FORBIDDEN);
    }
    return group;
  }

  public Group isOwnerForGroupUpdate(Long groupId, Long userId) {
    Group group = findByIdOrThrow(groupId);
    if(!group.getOwner().getId().equals(userId)) {
      throw new GroupException(GroupErrorCode.GROUP_UPDATE_FORBIDDEN);
    }
    return group;
  }
}
