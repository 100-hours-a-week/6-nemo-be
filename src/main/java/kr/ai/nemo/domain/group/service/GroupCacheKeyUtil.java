package kr.ai.nemo.domain.group.service;

import kr.ai.nemo.domain.group.dto.request.GroupSearchRequest;
import kr.ai.nemo.global.redis.CacheKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupCacheKeyUtil {

  public String getGroupListKey(GroupSearchRequest request, Pageable pageable) {
    return CacheKeyUtil.key(
        "group-list",
        "category", request.getCategory(),
        "page", pageable.getPageNumber(),
        "size", pageable.getPageSize()
    );
  }
}
