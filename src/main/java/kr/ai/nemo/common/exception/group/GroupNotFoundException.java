package kr.ai.nemo.common.exception.group;

import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;

public class GroupNotFoundException extends CustomException {
  public GroupNotFoundException() {
    super(ResponseCode.GROUP_NOT_FOUND);
  }
}
