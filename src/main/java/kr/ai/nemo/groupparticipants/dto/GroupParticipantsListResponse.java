package kr.ai.nemo.groupparticipants.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupParticipantsListResponse {
  private List<GroupParticipantDto> participants;
}
