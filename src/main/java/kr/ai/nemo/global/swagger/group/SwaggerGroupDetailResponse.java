package kr.ai.nemo.global.swagger.group;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ai.nemo.domain.group.dto.response.GroupDetailResponse;
import kr.ai.nemo.global.common.BaseApiResponse;

@Schema(name = "BaseApiResponse_GroupDetailResponse", description = "모임 상세 조회")
public class SwaggerGroupDetailResponse extends BaseApiResponse<GroupDetailResponse> {

}
