package kr.ai.nemo.global.swagger.group;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.domain.group.dto.response.GroupListResponse;

@Schema(name = "BaseApiResponse_GroupListResponse", description = "모임 리스트 응답")
public class SwaggerGroupListResponse extends BaseApiResponse<GroupListResponse> {

}
