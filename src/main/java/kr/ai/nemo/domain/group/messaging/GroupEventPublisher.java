package kr.ai.nemo.domain.group.messaging;

import kr.ai.nemo.domain.group.dto.response.GroupCreateResponse;

public interface GroupEventPublisher {
    void publishGroupCreated(GroupCreateResponse data);
    void publishGroupDeleted(Long groupId);
    void publishGroupJoined(Long userId, Long groupId);
    void publishGroupLeft(Long userId, Long groupId);
}
