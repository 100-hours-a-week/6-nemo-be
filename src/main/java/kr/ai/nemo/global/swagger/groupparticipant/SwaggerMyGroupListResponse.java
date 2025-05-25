package kr.ai.nemo.global.swagger.groupparticipant;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ai.nemo.domain.groupparticipants.dto.response.MyGroupListResponse;
import kr.ai.nemo.global.common.BaseApiResponse;

@Schema(name = "BaseApiResponse_MyGroupListResponse", description = "내 모임 리스트 조회")
public class SwaggerMyGroupListResponse extends BaseApiResponse<MyGroupListResponse> {

}
