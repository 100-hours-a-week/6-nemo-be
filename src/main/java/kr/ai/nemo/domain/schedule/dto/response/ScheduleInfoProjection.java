package kr.ai.nemo.domain.schedule.dto.response;

import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScheduleInfoProjection {
    private Long scheduleId;
    private String title;
    private String description;
    private String address;
    private ScheduleStatus status;
    private Integer currentUserCount;
    private Long groupId;
    private String groupName;
    private String ownerName;
    private String startAt;
    private ScheduleParticipantStatus participantStatus;

    // 기본 생성자 (필수)
    public ScheduleInfoProjection() {}

    // Hibernate용 생성자 (LocalDateTime을 받아서 String으로 변환)
    public ScheduleInfoProjection(Long scheduleId, String title, String description, 
                                 String address, ScheduleStatus status, Integer currentUserCount,
                                 Long groupId, String groupName, String ownerName, 
                                 LocalDateTime startAt, ScheduleParticipantStatus participantStatus) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.description = description;
        this.address = address;
        this.status = status;
        this.currentUserCount = currentUserCount;
        this.groupId = groupId;
        this.groupName = groupName;
        this.ownerName = ownerName;
        this.startAt = startAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.participantStatus = participantStatus;
    }

    // Getters
    public Long getScheduleId() { return scheduleId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public ScheduleStatus getStatus() { return status; }
    public Integer getCurrentUserCount() { return currentUserCount; }
    public Long getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public String getOwnerName() { return ownerName; }
    public String getStartAt() { return startAt; }
    public ScheduleParticipantStatus getParticipantStatus() { return participantStatus; }

    // Setters
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setAddress(String address) { this.address = address; }
    public void setStatus(ScheduleStatus status) { this.status = status; }
    public void setCurrentUserCount(Integer currentUserCount) { this.currentUserCount = currentUserCount; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setStartAt(String startAt) { this.startAt = startAt; }
    public void setParticipantStatus(ScheduleParticipantStatus participantStatus) { this.participantStatus = participantStatus; }
}
