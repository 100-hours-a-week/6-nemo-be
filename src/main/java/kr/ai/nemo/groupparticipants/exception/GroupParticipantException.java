package kr.ai.nemo.groupparticipants.exception;

import lombok.Getter;

@Getter
public class GroupParticipantException extends RuntimeException {
  private final GroupParticipantErrorCode errorCode;

  public GroupParticipantException(GroupParticipantErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
