package kr.ai.nemo.domain.groupparticipants.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "나의 모임 리스트 목록", description = "나의 모임 리스트 응답 DTO")
public record MyGroupListResponse(
    List<MyGroupDto> groups
) {}
