package kr.ai.nemo.domain.group.controller;

import kr.ai.nemo.domain.group.service.GroupCommandService;
import kr.ai.nemo.domain.group.service.GroupGenerateService;
import kr.ai.nemo.domain.group.service.GroupQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GroupController.class)
@ActiveProfiles("test")
@DisplayName("GroupController 테스트")
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupCommandService groupCommandService;

    @MockBean
    private GroupQueryService groupQueryService;

    @MockBean
    private GroupGenerateService groupGenerateService;

    @Test
    @DisplayName("그룹 생성 API 테스트")
    void createGroup_Success() {
        // given
        
        // when
        
        // then
    }
}
