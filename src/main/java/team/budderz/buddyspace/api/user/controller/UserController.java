package team.budderz.buddyspace.api.user.controller;

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
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public BaseResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return new BaseResponse<>(userService.signup(signupRequest));
    }

    @PostMapping("/login")
    public BaseResponse<TokenResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        return new BaseResponse<>(userService.login(loginRequest, response));
    }

    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        userService.logout(token);

        return new BaseResponse<>(null);
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<UserUpdateResponse> updateUser(
            @AuthenticationPrincipal UserAuth userAuth,
            @CookieValue(value = "verified_password", required = false) String passwordToken,
            HttpServletResponse response,
            @RequestPart("request") @Valid UserUpdateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return new BaseResponse<>(userService.updateUser(
                userAuth.getUserId(), passwordToken, response, request, profileImage
        ));
    }

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

    @DeleteMapping
    public BaseResponse<Void> deleteUser(
            @AuthenticationPrincipal UserAuth userAuth,
            @CookieValue(value = "verified_password", required = false) String passwordToken,
            HttpServletResponse response
    ) {
        userService.deleteUser(userAuth.getUserId(), passwordToken, response);

        return new BaseResponse<>(null);
    }

    @PostMapping("/verify-password")
    public BaseResponse<Void> verifyPassword(
            @AuthenticationPrincipal UserAuth userAuth,
            @Valid @RequestBody PasswordRequest request,
            HttpServletResponse response
    ) {
        userService.verifyPassword(userAuth.getUserId(), request, response);
        return new BaseResponse<>(null);
    }

    @GetMapping("/me")
    public BaseResponse<UserDetailResponse> getMyInfo(@AuthenticationPrincipal UserAuth userAuth) {
        return new BaseResponse<>(userService.getMyPage(userAuth.getUserId()));
    }
}
