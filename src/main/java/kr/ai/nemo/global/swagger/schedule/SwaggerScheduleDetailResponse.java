package kr.ai.nemo.global.swagger.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleDetailResponse;
import kr.ai.nemo.global.common.BaseApiResponse;

@Schema(name = "BaseApiResponse_ScheduleDetailResponse", description = "모임 상세 조회")
public class SwaggerScheduleDetailResponse extends BaseApiResponse<ScheduleDetailResponse> {

}
