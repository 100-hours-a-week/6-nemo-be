package kr.ai.nemo.group.participants.dto;

import java.util.List;

public record MyGroupListResponse(
    List<MyGroupDto> groups
) {}
