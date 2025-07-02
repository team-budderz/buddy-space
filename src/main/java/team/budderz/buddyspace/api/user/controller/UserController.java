package team.budderz.buddyspace.api.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.budderz.buddyspace.api.auth.response.TokenResponse;
import team.budderz.buddyspace.api.user.request.*;
import team.budderz.buddyspace.api.user.response.SignupResponse;
import team.budderz.buddyspace.api.user.response.UserDetailResponse;
import team.budderz.buddyspace.api.user.response.UserUpdateResponse;
import team.budderz.buddyspace.domain.user.service.UserService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "사용자 관리", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/signup")
    public BaseResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        SignupResponse response = userService.signup(signupRequest);
        return new BaseResponse<>(response);
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 수행합니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/login")
    public BaseResponse<TokenResponse> login(@RequestBody LoginRequest loginRequest,
                                             HttpServletResponse response) {
        TokenResponse tokenResponse = userService.login(loginRequest, response);
        return new BaseResponse<>(tokenResponse);
    }

    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자를 로그아웃합니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        userService.logout(token);

        return new BaseResponse<>(null);
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자의 프로필 정보를 수정합니다. (쿠키로 인증된 사용자만 수행 가능)")
    @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<UserUpdateResponse> updateUser(
            @AuthenticationPrincipal UserAuth userAuth,
            @CookieValue(value = "verified_password", required = false) String passwordToken,
            HttpServletResponse response,
            @RequestPart("request") @Valid UserUpdateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        UserUpdateResponse updateResponse =
                userService.updateUser(userAuth.getUserId(), passwordToken, response, request, profileImage);
        return new BaseResponse<>(updateResponse);
    }

    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다. (쿠키로 인증된 사용자만 수행 가능)")
    @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PatchMapping("/password")
    public BaseResponse<Void> updateUserPassword(
            @AuthenticationPrincipal UserAuth userAuth,
            @CookieValue(value = "verified_password", required = false) String passwordToken,
            HttpServletResponse response,
            @Valid @RequestBody UserPasswordUpdateRequest updateRequest
    ) {
        userService.updateUserPassword(userAuth.getUserId(), passwordToken, response, updateRequest);
        return new BaseResponse<>(null);
    }

    @Operation(summary = "회원 탈퇴", description = "사용자 계정을 삭제합니다. (쿠키로 인증된 사용자만 수행 가능)")
    @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping
    public BaseResponse<Void> deleteUser(
            @AuthenticationPrincipal UserAuth userAuth,
            @CookieValue(value = "verified_password", required = false) String passwordToken,
            HttpServletResponse response
    ) {
        userService.deleteUser(userAuth.getUserId(), passwordToken, response);
        return new BaseResponse<>(null);
    }

    @Operation(summary = "비밀번호 인증", description = "사용자 정보 및 비밀번호 변경, 탈퇴 전에 비밀번호를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "비밀번호 인증 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/verify-password")
    public BaseResponse<Void> verifyPassword(@AuthenticationPrincipal UserAuth userAuth,
                                             @Valid @RequestBody PasswordRequest request,
                                             HttpServletResponse response) {
        userService.verifyPassword(userAuth.getUserId(), request, response);
        return new BaseResponse<>(null);
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/me")
    public BaseResponse<UserDetailResponse> getMyInfo(@AuthenticationPrincipal UserAuth userAuth) {
        UserDetailResponse response = userService.getMyPage(userAuth.getUserId());
        return new BaseResponse<>(response);
    }
}
