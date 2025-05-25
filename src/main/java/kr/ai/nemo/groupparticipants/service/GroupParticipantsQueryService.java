package kr.ai.nemo.groupparticipants.service;

import java.util.List;
import kr.ai.nemo.group.domain.enums.GroupStatus;
import kr.ai.nemo.group.validator.GroupValidator;
import kr.ai.nemo.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.groupparticipants.domain.enums.Status;
import kr.ai.nemo.groupparticipants.dto.response.GroupParticipantsListResponse;
import kr.ai.nemo.groupparticipants.dto.response.MyGroupDto;
import kr.ai.nemo.groupparticipants.repository.GroupParticipantsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GroupParticipantsQueryService {

  private final GroupParticipantsRepository groupParticipantsRepository;
  private final GroupValidator groupValidator;

  public List<GroupParticipantsListResponse.GroupParticipantDto> getAcceptedParticipants(Long groupId) {
    groupValidator.findByIdOrThrow(groupId);
    return groupParticipantsRepository.findByGroupIdAndStatus(groupId, Status.JOINED).stream()
        .map(GroupParticipantsListResponse.GroupParticipantDto::from)
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
