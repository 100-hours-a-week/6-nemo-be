package kr.ai.nemo.domain.group.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.response.GroupDetailStaticInfo;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.global.redis.CacheJitterUtil;
import kr.ai.nemo.global.redis.CacheKeyUtil;
import kr.ai.nemo.global.redis.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupCacheService {

    private final GroupRepository groupRepository;
    private final GroupTagService groupTagService;
    private final RedisCacheService redisCacheService;
    private final GroupCacheKeyUtil groupCacheKeyUtil;

    private static final Duration groupCacheExpire = Duration.ofMinutes(3);
    private static final Duration errorCacheExpire = Duration.ofMinutes(2);

    private static class GroupCacheKeys {
        final String groupDetail;
        final String nullCache;
        final String disbanded;

        GroupCacheKeys(Long groupId) {
            this.groupDetail = CacheKeyUtil.key("group_detail", groupId);
            this.nullCache = CacheKeyUtil.key("group_null", groupId);
            this.disbanded = CacheKeyUtil.key("group_disbanded", groupId);
        }
    }

    public GroupDetailStaticInfo getGroupDetailStatic(Long groupId) {
        GroupCacheKeys keys = new GroupCacheKeys(groupId);

        // DISBANDED 상태 체크
        if (redisCacheService.get(keys.disbanded, String.class).isPresent() ||
            redisCacheService.isNullCached(keys.disbanded)) {
            log.info("Cache Hit (disbanded): groupId = {}", groupId);
            throw new GroupException(GroupErrorCode.GROUP_DISBANDED);
        }

        // 정상 데이터 조회
        Optional<GroupDetailStaticInfo> cacheResult = redisCacheService.get(keys.groupDetail, GroupDetailStaticInfo.class);
        if (cacheResult.isPresent()) {
            log.info("Cache Hit: groupId = {}", groupId);
            return cacheResult.get();
        }

        // NOT_FOUND 체크
        if (redisCacheService.isNullCached(keys.nullCache)) {
            log.info("Cache Hit (null value): groupId = {}", groupId);
            throw new GroupException(GroupErrorCode.GROUP_NOT_FOUND);
        }

        log.info("Cache Miss: groupId = {}", groupId);
        return fetchAndCacheGroupDetail(groupId, keys);
    }

    private GroupDetailStaticInfo fetchAndCacheGroupDetail(Long groupId, GroupCacheKeys keys) {
        try {
            Optional<Group> groupOpt = groupRepository.findByIdGroupId(groupId);

            if (groupOpt.isEmpty()) {
                redisCacheService.set(keys.nullCache, null, errorCacheExpire);
                log.info("Cache Set (not found): groupId = {}", groupId);
                throw new GroupException(GroupErrorCode.GROUP_NOT_FOUND);
            }

            Group group = groupOpt.get();

            if (group.getStatus() == GroupStatus.DISBANDED) {
                redisCacheService.set(keys.disbanded, GroupStatus.DISBANDED.toString(), errorCacheExpire);
                log.info("Cache Set (disbanded): groupId = {}", groupId);
                throw new GroupException(GroupErrorCode.GROUP_DISBANDED);
            }

            List<String> tags = groupTagService.getTagNamesByGroupId(group.getId());
            GroupDetailStaticInfo result = GroupDetailStaticInfo.from(group, tags);

            Duration jitteredExpire = CacheJitterUtil.addJitter(groupCacheExpire, 3);
            redisCacheService.set(keys.groupDetail, result, jitteredExpire);

            log.info("Cache Set (success): groupId = {}", groupId);

            return result;
        } catch (GroupException e) {
            throw e;
        } catch (Exception e) {
            redisCacheService.set(keys.nullCache, null, errorCacheExpire);
            log.error("Unexpected error for groupId = {}, treating as not found: {}", groupId, e.getMessage());
            throw new GroupException(GroupErrorCode.GROUP_NOT_FOUND);
        }
    }

    public void evictGroupDetailStatic(Long groupId) {
        GroupCacheKeys keys = new GroupCacheKeys(groupId);
        redisCacheService.del(keys.groupDetail);
        log.info("Cache Evict: groupId = {}", groupId);
    }

    public void deleteGroupListCaches() {
        try {
            String keys = groupCacheKeyUtil.getGroupListKey();
            if (keys != null && !keys.isEmpty()) {
                redisCacheService.del(keys);
            }
        } catch (Exception e) {
            log.error("Failed to delete group-list caches: {}", e.getMessage());
        }
    }
}
