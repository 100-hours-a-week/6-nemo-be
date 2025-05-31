package kr.ai.nemo.domain.group.service;

import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupCommandService 테스트")
class GroupCommandServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupValidator groupValidator;

    @Mock
    private GroupTagService groupTagService;

    @InjectMocks
    private GroupCommandService groupCommandService;

    @Test
    @DisplayName("그룹 생성 성공 테스트")
    void createGroup_Success() {
        // given
        
        // when
        
        // then
    }
}
