package com.project.board0905.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class AuthDtos {
    @Getter
    @Setter
    public static class LoginReq {
        @NotBlank
        private String username;
        @NotBlank private String password;
    }
    @Builder
    @Getter
    public static class TokenRes {
        private final String accessToken;
        private final String refreshToken;
        private final long   expiresInSec;
        private final String tokenType; // "Bearer"
    }
    @Getter @Setter
    public static class RefreshReq {
        @NotBlank private String refreshToken;
    }
}
