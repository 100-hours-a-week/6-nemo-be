package kr.ai.nemo.global.swagger.group;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleListResponse;
import kr.ai.nemo.global.common.BaseApiResponse;

@Schema(name = "BaseApiResponse_ScheduleListResponse", description = "모임의 일정 리스트 응답")
public class SwaggerScheduleListResponse extends BaseApiResponse<ScheduleListResponse> {

}
