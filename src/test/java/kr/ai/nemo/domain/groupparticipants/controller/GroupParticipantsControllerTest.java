package kr.ai.nemo.domain.groupparticipants.controller;

import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsCommandService;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GroupParticipantsController.class)
@ActiveProfiles("test")
@DisplayName("GroupParticipantsController 테스트")
class GroupParticipantsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupParticipantsCommandService groupParticipantsCommandService;

    @MockBean
    private GroupParticipantsQueryService groupParticipantsQueryService;

    @Test
    @DisplayName("그룹 참가 API 테스트")
    void joinGroup_Success() {
        // given
        
        // when
        
        // then
    }
}
