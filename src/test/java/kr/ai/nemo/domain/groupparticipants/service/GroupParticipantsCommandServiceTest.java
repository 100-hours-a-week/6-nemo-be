package kr.ai.nemo.domain.groupparticipants.service;

import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupParticipantsCommandService 테스트")
class GroupParticipantsCommandServiceTest {

    @Mock
    private GroupParticipantsRepository groupParticipantsRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupParticipantValidator groupParticipantValidator;

    @InjectMocks
    private GroupParticipantsCommandService groupParticipantsCommandService;

    @Test
    @DisplayName("그룹 참가 성공 테스트")
    void joinGroup_Success() {
        // given
        
        // when
        
        // then
    }
}
