package kr.ai.nemo.integration.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import kr.ai.nemo.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통합 테스트를 위한 기본 설정 클래스
 * 모든 통합 테스트는 이 클래스를 상속받아 사용
 */
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@Import(JwtProvider.class)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    protected GroupParticipantsRepository groupParticipantsRepository;

    /**
     * 테스트 데이터 정리
     */
    protected void clearDatabase() {
        entityManager.flush();
        entityManager.clear();
    }
}
