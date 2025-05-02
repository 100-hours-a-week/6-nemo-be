package kr.ai.nemo.common.exception.group;

import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ErrorCode;

public class GroupNotFoundException extends CustomException {
  public GroupNotFoundException() {
    super(ErrorCode.GROUP_NOT_FOUND);
  }
}
