package com.quartzbatchcontrol.user.api;

import com.quartzbatchcontrol.global.response.ApiResponse;
import com.quartzbatchcontrol.user.api.request.LoginRequest;
import com.quartzbatchcontrol.user.api.request.SignUpRequest;
import com.quartzbatchcontrol.user.api.response.LoginResponse;
import com.quartzbatchcontrol.user.application.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        String accessToken = authService.login(request);

        LoginResponse loginResponse = LoginResponse.builder()
                .email(request.getEmail())
                .accessToken(accessToken)
                .build();

        return ResponseEntity.ok(ApiResponse.success(loginResponse, "로그인이 완료되었습니다."));
    }

}