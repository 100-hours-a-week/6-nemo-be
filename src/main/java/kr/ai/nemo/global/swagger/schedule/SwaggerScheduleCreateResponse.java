package kr.ai.nemo.global.swagger.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleCreateResponse;
import kr.ai.nemo.global.common.BaseApiResponse;

@Schema(name = "BaseApiResponse_ScheduleCreateResponse", description = "일정 생성 응답")
public class SwaggerScheduleCreateResponse extends BaseApiResponse<ScheduleCreateResponse> {

}
