package kr.ai.nemo.global.swagger.groupparticipant;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ai.nemo.domain.groupparticipants.dto.response.GroupParticipantsListResponse;
import kr.ai.nemo.global.common.BaseApiResponse;

@Schema(name = "BaseApiResponse_GroupParticipantsListResponse", description = "모임원 리스트 조회")
public class SwaggerGroupParticipantsListResponse extends BaseApiResponse<GroupParticipantsListResponse> {

}
