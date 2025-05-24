package kr.ai.nemo.groupparticipants.dto;

import java.util.List;

public record MyGroupListResponse(
    List<MyGroupDto> groups
) {}
