package kr.ai.nemo.domain.groupparticipants.dto.response;

import java.util.List;

public record MyGroupListResponse(
    List<MyGroupDto> groups
) {}
