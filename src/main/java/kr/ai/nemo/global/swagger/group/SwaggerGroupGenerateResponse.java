package kr.ai.nemo.global.swagger.group;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ai.nemo.domain.group.dto.response.GroupGenerateResponse;
import kr.ai.nemo.global.common.BaseApiResponse;

@Schema(name = "BaseApiResponse_GroupGenerateResponse", description = "AI가 생성해준 모임 정보 응답")
public class SwaggerGroupGenerateResponse extends BaseApiResponse<GroupGenerateResponse> {

}
