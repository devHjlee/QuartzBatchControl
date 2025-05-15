package com.quartzbatchcontrol.user.application;

import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import com.quartzbatchcontrol.global.security.jwt.JwtTokenProvider;
import com.quartzbatchcontrol.user.api.request.LoginRequest;
import com.quartzbatchcontrol.user.api.request.SignUpRequest;
import com.quartzbatchcontrol.user.domain.User;
import com.quartzbatchcontrol.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public void signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.createUser(
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getName()
        );

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            return tokenProvider.createToken(authentication);
        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for user: {}", request.getEmail(), e);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getEmail(), e);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", request.getEmail(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

} 