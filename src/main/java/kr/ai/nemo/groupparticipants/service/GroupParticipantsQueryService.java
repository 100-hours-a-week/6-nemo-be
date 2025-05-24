package kr.ai.nemo.groupparticipants.service;

import java.util.List;
import kr.ai.nemo.group.domain.enums.GroupStatus;
import kr.ai.nemo.group.validator.GroupValidator;
import kr.ai.nemo.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.groupparticipants.domain.enums.Status;
import kr.ai.nemo.groupparticipants.dto.GroupParticipantDto;
import kr.ai.nemo.groupparticipants.dto.MyGroupDto;
import kr.ai.nemo.groupparticipants.repository.GroupParticipantsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GroupParticipantsQueryService {

  private final GroupParticipantsRepository groupParticipantsRepository;
  private final GroupValidator groupValidator;

  public List<GroupParticipantDto> getAcceptedParticipants(Long groupId) {
    groupValidator.findByIdOrThrow(groupId);
    List<GroupParticipants> participants = groupParticipantsRepository.findByGroupIdAndStatus(groupId, Status.JOINED);
    return participants.stream()
        .map(GroupParticipantDto::from)
        .toList();
  }

  public List<MyGroupDto> getMyGroups(Long userId) {
    List<GroupParticipants> participants = groupParticipantsRepository.findByUserIdAndStatus(userId, Status.JOINED);

    return participants.stream()
        .map(GroupParticipants::getGroup)
        .filter(group -> group.getStatus() != GroupStatus.DISBANDED)
        .map(MyGroupDto::from)
        .toList();
  }
}
