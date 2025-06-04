package kr.ai.nemo.domain.schedule.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.schedule.ScheduleFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@DisplayName("ScheduleRepository 테스트")
class ScheduleRepositoryTest {

  @Autowired
  private ScheduleRepository scheduleRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private GroupRepository groupRepository;


  ScheduleStatus recruitingStatus = ScheduleStatus.RECRUITING;
  ScheduleStatus canceledStatus = ScheduleStatus.CANCELED;
  User savedUser;
  Group savedGroup;
  //취소된 일정
  Schedule schedule1;
  Schedule schedule2;
  //진행중인 일정
  Schedule schedule3;
  Schedule schedule4;
  Schedule schedule5;

  @BeforeEach
  void setUp() {

    User user = UserFixture.createDefaultUser();
    savedUser = userRepository.save(user);

    Group group = GroupFixture.createDefaultGroup(savedUser);
    savedGroup = groupRepository.save(group);

    // 상태가 CANCELED인 일정 2개
    schedule1 = ScheduleFixture.createDefaultSchedule(savedGroup, savedUser,
        ScheduleStatus.CANCELED);
    schedule2 = ScheduleFixture.createDefaultSchedule(savedGroup, savedUser,
        ScheduleStatus.CANCELED);

    // 상태가 RECRUITING인 일정 3개
    schedule3 = ScheduleFixture.createDefaultSchedule(savedGroup, savedUser,
        ScheduleStatus.RECRUITING);
    schedule4 = ScheduleFixture.createDefaultSchedule(savedGroup, savedUser,
        ScheduleStatus.RECRUITING);
    schedule5 = ScheduleFixture.createDefaultSchedule(savedGroup, savedUser,
        ScheduleStatus.RECRUITING);

    scheduleRepository.saveAll(List.of(schedule1, schedule2, schedule3, schedule4, schedule5));

  }

  @Test
  @DisplayName("[성공] 일정 저장 테스트")
  void save_Success() {
    // then
    assertThat(scheduleRepository.count()).isEqualTo(2);
    assertThat(schedule1.getId()).isEqualTo(1L);
    assertThat(schedule1.getStatus()).isEqualTo(canceledStatus);
  }

  @Test
  @DisplayName("[성공] 모임의 일정 list 조회 테스트")
  void findByGroupIdAndStatusNot() {
    // when
    PageRequest pageRequest = PageRequest.of(0, 10);
    Page<Schedule> page = scheduleRepository.findByGroupIdAndStatusNot(savedGroup.getId(),
        pageRequest, canceledStatus);

    // then
    List<Schedule> schedules = page.getContent();

    assertThat(schedules).hasSize(3);
    assertThat(schedules.get(0)).isEqualTo(schedule3);
    assertThat(schedules.get(1)).isEqualTo(schedule4);
    assertThat(schedules.get(2)).isEqualTo(schedule5);
    assertThat(schedules.getFirst().getStatus()).isEqualTo(ScheduleStatus.RECRUITING);
    assertThat(page.getTotalElements()).isEqualTo(3);
  }

  @Test
  @DisplayName("[성공] 모임의 진행 전 일정 list 조회 테스트")
  void findByGroupIdAndStatus_Success() {
    // when
    List<Schedule> result = scheduleRepository.findByGroupIdAndStatus(savedGroup.getId(),
        recruitingStatus);

    // then
    assertThat(result).hasSize(3);
    assertThat(result.getFirst().getStatus()).isEqualTo(recruitingStatus);
    assertThat(result.getFirst().getId()).isEqualTo(schedule3.getId());
    assertThat(result.getLast().getStatus()).isEqualTo(recruitingStatus);
  }

  @Test
  @DisplayName("[성공] 종료된 일정 list - 스케줄러 동작 테스트")
  void findByStartAtBeforeAndStatus() {
    // when
    List<Schedule> result = scheduleRepository.findByStartAtBeforeAndStatus(LocalDateTime.now(),
        recruitingStatus);

    // then
    assertThat(result).hasSize(3);
    assertThat(result.getFirst().getStatus()).isEqualTo(recruitingStatus);
    assertThat(result.getFirst().getId()).isEqualTo(schedule3.getId());
    assertThat(result.getLast().getStatus()).isEqualTo(recruitingStatus);
  }

  @Test
  @DisplayName("[성공] 일정 상세 조회 테스트 (일정 생성자와 일정의 모임도 함께 가져오는 경우)")
  void findByIdWithGroupAndOwner() {
    // when
    Optional<Schedule> result = scheduleRepository.findById(1L);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getGroup().getId()).isEqualTo(savedGroup.getId());
    assertThat(result.get().getOwner().getId()).isEqualTo(savedUser.getId());
  }

  @Test
  @DisplayName("[성공] 일정 생성자인지 검증 테스트")
  void existsByIdAndOwnerId() {
    // when
    boolean exists = scheduleRepository.existsByIdAndOwnerId(1L, 1L);
    boolean exists2 = scheduleRepository.existsByIdAndOwnerId(2L, 2L);

    // then
    assertThat(exists).isTrue();
    assertThat(exists2).isFalse();
  }
}
