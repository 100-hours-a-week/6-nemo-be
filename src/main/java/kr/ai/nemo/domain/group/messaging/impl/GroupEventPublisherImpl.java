package kr.ai.nemo.domain.group.messaging.impl;

import kr.ai.nemo.domain.group.dto.response.GroupCreateResponse;
import kr.ai.nemo.domain.group.messaging.GroupEventPublisher;
import kr.ai.nemo.domain.groupparticipants.dto.request.GroupParticipantAiRequest;
import kr.ai.nemo.global.kafka.utils.GroupEvent;
import kr.ai.nemo.global.kafka.utils.KafkaTopic;
import kr.ai.nemo.global.kafka.messaging.MessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupEventPublisherImpl implements GroupEventPublisher {
    
    private final MessagePublisher messagePublisher;
    
    @Override
    public void publishGroupCreated(GroupCreateResponse data) {
        GroupEvent event = GroupEvent.groupCreated(data);
        String key = UUID.randomUUID().toString();
        
        messagePublisher.publishAsync(KafkaTopic.GROUP_EVENT.getName(), key, event)
            .thenRun(() -> log.info("[GROUP_CREATED] Event published: {}", event))
            .exceptionally(ex -> {
                log.error("[GROUP_CREATED] Failed to publish event: {}", event, ex);
                return null;
            });
    }
    
    @Override
    public void publishGroupDeleted(Long groupId) {
        GroupEvent event = GroupEvent.groupDeleted(groupId);
        String key = UUID.randomUUID().toString();
        
        messagePublisher.publishAsync(KafkaTopic.GROUP_EVENT.getName(), key, event)
            .thenRun(() -> log.info("[GROUP_DELETED] Event published for groupId: {}", groupId))
            .exceptionally(ex -> {
                log.error("[GROUP_DELETED] Failed to publish event for groupId: {}", groupId, ex);
                return null;
            });
    }
    
    @Override
    public void publishGroupJoined(Long userId, Long groupId) {
        GroupParticipantAiRequest data = new GroupParticipantAiRequest(userId, groupId);
        GroupEvent event = GroupEvent.userJoined(data);
        String key = UUID.randomUUID().toString();
        
        messagePublisher.publishAsync(KafkaTopic.GROUP_EVENT.getName(), key, event)
            .thenRun(() -> log.info("[GROUP_JOINED] Event published: {}", event))
            .exceptionally(ex -> {
                log.error("[GROUP_JOINED] Failed to publish event: {}", event, ex);
                return null;
            });
    }
    
    @Override
    public void publishGroupLeft(Long userId, Long groupId) {
        GroupParticipantAiRequest data = new GroupParticipantAiRequest(userId, groupId);
        GroupEvent event = GroupEvent.userLeft(data);
        String key = UUID.randomUUID().toString();
        
        messagePublisher.publishAsync(KafkaTopic.GROUP_EVENT.getName(), key, event)
            .thenRun(() -> log.info("[GROUP_LEFT] Event published: {}", event))
            .exceptionally(ex -> {
                log.error("[GROUP_LEFT] Failed to publish event: {}", event, ex);
                return null;
            });
    }
}
