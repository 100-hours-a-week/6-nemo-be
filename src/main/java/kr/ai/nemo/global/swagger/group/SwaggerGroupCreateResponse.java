package kr.ai.nemo.global.swagger.group;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ai.nemo.domain.group.dto.response.GroupCreateResponse;
import kr.ai.nemo.global.common.BaseApiResponse;

@Schema(name = "BaseApiReponse_GroupCreateResponse", description = "모임 생성 응답")
public class SwaggerGroupCreateResponse extends BaseApiResponse<GroupCreateResponse> {

}
