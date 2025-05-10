package kr.ai.nemo.group.dto;

public record GroupGenerateRequest(
    String name,
    String goal,
    String category,
    String location,
    String period,
    int maxUserCount,
    boolean isPlanCreated
) {}
