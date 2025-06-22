package kr.ai.nemo.domain.group.service;

import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.dto.response.GroupDetailStaticInfo;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupCacheService {

    private final GroupValidator groupValidator;
    private final GroupTagService groupTagService;

    @Cacheable(value = "group-detail", key = "#groupId")
    public GroupDetailStaticInfo getGroupDetailStatic(Long groupId) {
        Group group = groupValidator.findByIdOrThrow(groupId);
        List<String> tags = groupTagService.getTagNamesByGroupId(group.getId());
        return GroupDetailStaticInfo.from(group, tags);
    }

    @CacheEvict(value = "group-detail", key = "#groupId")
    public void evictGroupDetailStatic(Long groupId) {
        // 캐시 무효화 전용 메서드
    }
}
