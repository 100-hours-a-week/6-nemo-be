package kr.ai.nemo.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ai.nemo.unit.global.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.user.dto.MyPageResponse;
import kr.ai.nemo.domain.user.dto.NicknameUpdateRequest;
import kr.ai.nemo.domain.user.dto.UpdateUserImageRequest;
import kr.ai.nemo.domain.user.service.UserService;
import kr.ai.nemo.unit.global.common.BaseApiResponse;
import kr.ai.nemo.unit.global.swagger.group.SwaggerGroupListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 API", description = "사용자 관련 API 입니다.")
@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @Operation(summary = "마이페이지 조회", description = "마이페이지를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerGroupListResponse.class)))
  @TimeTrace
  @GetMapping("/me")
  public ResponseEntity<BaseApiResponse<MyPageResponse>> myPage(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(BaseApiResponse.success(userService.getMyPage(userDetails.getUserId())));
  }

  @Operation(summary = "닉네임 변경", description = "사용자의 닉네임을 변경합니다.")
  @ApiResponse(responseCode = "200", description = "성공적으로 처리되었습니다.")
  @TimeTrace
  @PatchMapping("/me/nickname")
  public ResponseEntity<BaseApiResponse<MyPageResponse>> updateNickname(
      @Valid @RequestBody NicknameUpdateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    MyPageResponse response = userService.updateMyNickname(userDetails.getUserId(), request);
    return ResponseEntity.ok(BaseApiResponse.success(response));
  }

  @Operation(summary = "프로필 사진 변경", description = "사용자 프로필 사진을 변경합니다.")
  @ApiResponse(responseCode = "200", description = "성공적으로 처리되었습니다.")
  @TimeTrace
  @PatchMapping("/me/profile-image")
  public ResponseEntity<BaseApiResponse<MyPageResponse>> updateProfileImage(
      @RequestBody UpdateUserImageRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    MyPageResponse response = userService.updateUserImage(userDetails.getUserId(), request);
    return ResponseEntity.ok(BaseApiResponse.success(response));
  }
}
