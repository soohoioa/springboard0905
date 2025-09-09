package com.project.board0905.auth.controller;

import com.project.board0905.auth.service.AuthService;
import com.project.board0905.auth.jwt.JwtProvider;
import com.project.board0905.auth.dto.AuthDtos;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtProvider jwt;

    @PostMapping("/login")
    public AuthDtos.TokenRes login(@Valid @RequestBody AuthDtos.LoginReq req) {
        return authService.login(req.getUsername(), req.getPassword());
    }

    @PostMapping("/refresh")
    public AuthDtos.TokenRes refresh(@Valid @RequestBody AuthDtos.RefreshReq req) {
        return authService.refresh(req.getRefreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String at = header.substring(7);
            String username = jwt.getUsername(at);
            authService.logout(at, username);
        }
        return ResponseEntity.noContent().build();
    }
}
