package kr.ai.nemo.domain.groupparticipants.exception;

import lombok.Getter;

@Getter
public class GroupParticipantException extends RuntimeException {
  private final GroupParticipantErrorCode errorCode;

  public GroupParticipantException(GroupParticipantErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
