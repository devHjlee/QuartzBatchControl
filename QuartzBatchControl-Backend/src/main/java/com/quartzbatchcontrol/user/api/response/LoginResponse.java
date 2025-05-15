package com.quartzbatchcontrol.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String email;
    private String accessToken;
    private final String tokenType = "Bearer";
} 