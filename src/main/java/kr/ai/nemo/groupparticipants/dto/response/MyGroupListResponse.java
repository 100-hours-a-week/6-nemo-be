package kr.ai.nemo.groupparticipants.dto.response;

import java.util.List;

public record MyGroupListResponse(
    List<MyGroupDto> groups
) {}
