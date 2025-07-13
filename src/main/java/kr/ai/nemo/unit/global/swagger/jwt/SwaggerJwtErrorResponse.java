package kr.ai.nemo.unit.global.swagger.jwt;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import kr.ai.nemo.unit.global.common.BaseApiResponse;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "401", description = "토큰이 유효하지 않습니다./토큰이 만료되었습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
public @interface SwaggerJwtErrorResponse {}
