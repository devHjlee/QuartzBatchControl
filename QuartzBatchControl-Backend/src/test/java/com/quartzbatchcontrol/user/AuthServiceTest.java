package com.quartzbatchcontrol.user;

import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import com.quartzbatchcontrol.global.security.jwt.JwtTokenProvider;
import com.quartzbatchcontrol.user.api.request.LoginRequest;
import com.quartzbatchcontrol.user.api.request.SignUpRequest;
import com.quartzbatchcontrol.user.application.AuthService;
import com.quartzbatchcontrol.user.domain.User;
import com.quartzbatchcontrol.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private SignUpRequest signUpRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setup() {
        signUpRequest = new SignUpRequest("test@example.com", "Password1!", "홍길동");
        loginRequest = new LoginRequest("test@example.com", "Password1!");
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() {
        // userRepository.existsByEmail()이 false를 반환하도록 설정
        given(userRepository.existsByEmail(anyString())).willReturn(false);

        // 비밀번호 인코딩된 값 반환
        given(passwordEncoder.encode(anyString())).willReturn("encoded-password");

        // 실제 테스트 실행
        authService.signUp(signUpRequest);

        // userRepository.save()가 호출되었는지 검증
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 실패")
    void signUp_duplicateEmail() {
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        assertThatThrownBy(() -> authService.signUp(signUpRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.DUPLICATE_EMAIL.getMessage());
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        Authentication mockAuth = mock(Authentication.class);

        given(authenticationManager.authenticate(any())).willReturn(mockAuth);
        given(jwtTokenProvider.createToken(any())).willReturn("jwt-token");

        String token = authService.login(loginRequest);

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 실패")
    void login_invalidCredentials() {
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_CREDENTIALS.getMessage());
    }

    @Test
    @DisplayName("알 수 없는 예외로 로그인 실패")
    void login_unexpectedError() {
        given(authenticationManager.authenticate(any()))
                .willThrow(new RuntimeException("unexpected"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }
}

