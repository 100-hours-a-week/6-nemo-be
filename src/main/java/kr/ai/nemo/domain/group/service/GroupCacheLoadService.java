package kr.ai.nemo.domain.group.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.request.GroupSearchRequest;
import kr.ai.nemo.domain.group.dto.response.GroupDto;
import kr.ai.nemo.domain.group.dto.response.GroupListResponse;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.global.aop.role.annotation.DistributedLock;
import kr.ai.nemo.global.redis.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupCacheLoadService {

    private final GroupRepository groupRepository;
    private final RedisCacheService redisCacheService;

    // DB 조회 및 캐시 갱신 (락 적용)
    @DistributedLock(
        key = "'group-list-lock::category:' + (#request.category == null ? 'null' : #request.category) + " +
            "':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize",
        waitTime = 0,
        leaseTime = 10,
        timeUnit = TimeUnit.SECONDS
    )
    @Transactional(readOnly = true)
    public GroupListResponse loadFromDbAndCache(GroupSearchRequest request, Pageable pageable, String cacheKey) {

        // double-checked locking: 락 획득 후 캐시 재확인
        Optional<GroupListResponse> cached = redisCacheService.get(cacheKey, GroupListResponse.class);
        if (cached.isPresent()) {
            log.info("락 획득 후 캐시 발견, 반환");
            return cached.get();
        }

        // DB 조회
        Page<Long> groupIdPage = queryDatabase(request, pageable);

        List<Group> groups = groupRepository.findGroupsWithTagsByIds(groupIdPage.getContent());
        List<GroupDto> dtos = groups.stream()
            .map(GroupDto::from)
            .toList();

        GroupListResponse result = GroupListResponse.from(
            new PageImpl<>(dtos, pageable, groupIdPage.getTotalElements()));

        // 캐시에 저장
        redisCacheService.set(cacheKey, result, Duration.ofMinutes(5));
        log.info("DB 조회 완료 및 캐시 저장");

        return result;
    }

    private Page<Long> queryDatabase(GroupSearchRequest request, Pageable pageable) {
        if (request.getCategory() != null) {
            log.info("캐시 미스로 인한 카테고리별 DB 조회");
            return groupRepository.findGroupIdsByCategoryAndStatusNot(
                request.getCategory(), GroupStatus.DISBANDED, pageable);
        } else if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            return groupRepository.searchGroupIdsWithKeywordOnly(
                request.getKeyword(), pageable);
        } else {
            log.info("캐시 미스로 인한 전체 모임 DB 조회");
            return groupRepository.findGroupIdsByStatusNot(pageable);
        }
    }
}
