package kr.ai.nemo.global.swagger.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ai.nemo.domain.schedule.dto.response.MySchedulesResponse;
import kr.ai.nemo.global.common.BaseApiResponse;

@Schema(name = "BaseApiResponse_MySchedulesResponse", description = "나의 일정 리스트 조회")
public class SwaggerMySchedulesResponse extends BaseApiResponse<MySchedulesResponse> {

}
