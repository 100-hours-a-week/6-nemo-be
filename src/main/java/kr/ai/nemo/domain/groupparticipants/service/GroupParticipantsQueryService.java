package kr.ai.nemo.domain.groupparticipants.service;

import java.util.List;
import kr.ai.nemo.global.aop.logging.TimeTrace;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.dto.response.GroupParticipantsListResponse;
import kr.ai.nemo.domain.groupparticipants.dto.response.MyGroupDto;
import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GroupParticipantsQueryService {

  private final GroupParticipantsRepository groupParticipantsRepository;
  private final GroupValidator groupValidator;

  @TimeTrace
  @Transactional(readOnly = true)
  public List<GroupParticipantsListResponse.GroupParticipantDto> getAcceptedParticipants(Long groupId) {
    groupValidator.findByIdOrThrow(groupId);
    return groupParticipantsRepository.findByGroupIdAndStatus(groupId, Status.JOINED).stream()
        .map(GroupParticipantsListResponse.GroupParticipantDto::from)
        .toList();
  }

  @TimeTrace
  @Transactional(readOnly = true)
  public List<MyGroupDto> getMyGroups(Long userId) {
    List<GroupParticipants> participants = groupParticipantsRepository.findByUserIdAndStatus(userId, Status.JOINED);

    return participants.stream()
        .map(GroupParticipants::getGroup)
        .filter(group -> group.getStatus() != GroupStatus.DISBANDED)
        .map(MyGroupDto::from)
        .toList();
  }
}
