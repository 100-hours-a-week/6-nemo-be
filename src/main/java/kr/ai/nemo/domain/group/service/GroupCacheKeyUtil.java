package kr.ai.nemo.domain.group.service;

import kr.ai.nemo.unit.global.redis.CacheKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupCacheKeyUtil {

  public String getGroupListKey() {
    return CacheKeyUtil.key(
        "group-list"
    );
  }
}
