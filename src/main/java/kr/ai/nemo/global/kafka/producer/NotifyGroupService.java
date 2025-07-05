package kr.ai.nemo.global.kafka.producer;

import kr.ai.nemo.domain.group.dto.response.GroupCreateResponse;
import kr.ai.nemo.domain.groupparticipants.dto.request.GroupParticipantAiRequest;
import kr.ai.nemo.global.kafka.utils.GroupEvent;
import kr.ai.nemo.global.kafka.utils.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyGroupService {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void notifyGroupCreated(GroupCreateResponse data) {
    GroupEvent event = GroupEvent.groupCreated(data);
    sendEvent(event);
    log.info("[Kafka][GROUP_CREATED] Sent to topic {}: {}",
        KafkaTopic.GROUP_EVENT.getName(), event);
  }

  public void notifyGroupDeleted(Long groupId) {
    GroupEvent event = GroupEvent.groupDeleted(groupId);
    sendEvent(event);
    log.info("[Kafka][GROUP_DELETED] Sent to topic {}: {}",
        KafkaTopic.GROUP_EVENT.getName(), groupId);
  }

  public void notifyGroupJoined(Long userId, Long groupId) {
    GroupParticipantAiRequest data = new GroupParticipantAiRequest(userId, groupId);
    GroupEvent event = GroupEvent.userJoined(data);
    sendEvent(event);
    log.info("[Kafka][GROUP_JOINED] Sent to topic {}: {}",
        KafkaTopic.GROUP_EVENT.getName(), event);
  }

  public void notifyGroupLeft(Long userId, Long groupId) {
    GroupParticipantAiRequest data = new GroupParticipantAiRequest(userId, groupId);
    GroupEvent event = GroupEvent.userLeft(data);
    sendEvent(event);
    log.info("[Kafka][GROUP_LEFT] Sent to topic {}: {}",
        KafkaTopic.GROUP_EVENT.getName(), event);
  }

  private void sendEvent(GroupEvent event) {
    try {
      kafkaTemplate.send(KafkaTopic.GROUP_EVENT.getName(), event)
          .whenComplete((result, ex) -> {
            if (ex == null) {
              log.info("✅ [Kafka] Event sent: {}", event.eventType());
            } else {
              log.error("❌ [Kafka] Failed to send event: {}", event, ex);
            }
          });
    } catch (Exception e) {
      log.error("❌ [Kafka] Error sending event: {}", event, e);
    }
  }
}
