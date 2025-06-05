package team.budderz.buddyspace.api.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.user.request.*;
import team.budderz.buddyspace.api.user.response.LoginResponse;
import team.budderz.buddyspace.api.user.response.SignupResponse;
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
    public BaseResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return new BaseResponse<>(userService.login(loginRequest));
    }

    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        userService.logout(token);

        return new BaseResponse<>(null);
    }

    @PatchMapping
    public BaseResponse<UserUpdateResponse> updateUser(
            @AuthenticationPrincipal UserAuth userAuth,
            @Valid @RequestBody UserUpdateRequest updateRequest
    ) {
        return new BaseResponse<>(userService.updateUser(userAuth, updateRequest));
    }

    @PatchMapping("/password")
    public BaseResponse<Void> updateUserPassword(
            @AuthenticationPrincipal UserAuth userAuth,
            @Valid @RequestBody UserPasswordUpdateRequest updateRequest
    ) {
        userService.updateUserPassword(userAuth, updateRequest);

        return new BaseResponse<>(null);
    }

    @DeleteMapping
    public BaseResponse<Void> deleteUser(
            @AuthenticationPrincipal UserAuth userAuth,
            @Valid @RequestBody UserDeleteRequest deleteRequest
    ) {
        userService.deleteUser(userAuth, deleteRequest);

        return new BaseResponse<>(null);
    }

    //소셜로그인 테스트
    @GetMapping("/me")
    public BaseResponse<SignupResponse> getMyInfo(@AuthenticationPrincipal UserAuth userAuth) {
        return new BaseResponse<>(userService.getMyPage(userAuth.getUserId()));
    }

}
