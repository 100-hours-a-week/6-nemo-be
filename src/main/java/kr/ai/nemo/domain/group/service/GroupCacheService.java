package kr.ai.nemo.domain.group.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.dto.response.GroupDetailStaticInfo;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.global.redis.CacheKeyUtil;
import kr.ai.nemo.global.redis.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupCacheService {

    private final GroupValidator groupValidator;
    private final GroupTagService groupTagService;
    private final RedisCacheService redisCacheService;

    private static final Duration groupCacheExpire = Duration.ofMinutes(10);
    private static final Duration nullGroupCacheExpire = Duration.ofMinutes(5);
    public GroupDetailStaticInfo getGroupDetailStatic(Long groupId) {
        String groupCacheKey = CacheKeyUtil.key("group_detail", groupId);

        Optional<GroupDetailStaticInfo> cacheResult = redisCacheService.get(groupCacheKey, GroupDetailStaticInfo.class);

        if (cacheResult.isPresent()) {
            log.info("Cache Hit: groupId = {}", groupId);
            return cacheResult.get();
        }

        if (redisCacheService.isNullCached(groupCacheKey)) {
            log.info("Cache Hit (null value): groupId = {}", groupId);
            throw new GroupException(GroupErrorCode.GROUP_NOT_FOUND);
        }


        log.info("Cache Miss: groupId = {}", groupId);
        return fetchAndCacheGroupDetail(groupId, groupCacheKey);
    }


    private GroupDetailStaticInfo fetchAndCacheGroupDetail(Long groupId, String groupCacheKey) {
        try {
            Group group = groupValidator.findByIdOrThrow(groupId);
            List<String> tags = groupTagService.getTagNamesByGroupId(group.getId());
            GroupDetailStaticInfo result = GroupDetailStaticInfo.from(group, tags);

            redisCacheService.set(groupCacheKey, result, groupCacheExpire);
            log.debug("Cache Set: groupId = {}", groupId);

            return result;
        } catch (Exception e) {
            redisCacheService.set(groupCacheKey, null, nullGroupCacheExpire);
            log.debug("Cache Set (null) groupId = {}", groupId);
            throw e;
        }
    }

    @CacheEvict(value = "group-detail-static", key = "#groupId")
    public void evictGroupDetailStatic(Long groupId) {
        // 캐시 무효화 전용 메서드
    }
}
