package com.project.board0905.auth.service;

import com.project.board0905.auth.jwt.JwtProperties;
import com.project.board0905.auth.jwt.JwtProvider;
import com.project.board0905.auth.dto.AuthDtos;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authManager;
    private final JwtProvider jwt;
    private final JwtProperties props;
    private final TokenStore tokenStore;

    public AuthDtos.TokenRes login(String username, String rawPassword) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, rawPassword));

        String role = auth.getAuthorities().stream().findFirst().orElseThrow().getAuthority().replace("ROLE_","");
        String at = jwt.createAccessToken(username, role);
        String rt = jwt.createRefreshToken(username);
        tokenStore.saveRefresh(username, rt, props.getRefreshExpDays());

        long expSec = props.getAccessExpMin()*60;
        return AuthDtos.TokenRes.builder().accessToken(at).refreshToken(rt).expiresInSec(expSec).tokenType("Bearer").build();
    }

    public AuthDtos.TokenRes refresh(String refreshToken) {
        // 1) 파싱 & 만료 체크
        if (jwt.isExpired(refreshToken)) throw new BadCredentialsException("refresh expired");
        String username = jwt.getUsername(refreshToken);

        // 2) Redis 저장값과 일치 검증 (탈취 방지)
        String saved = tokenStore.getRefresh(username).orElseThrow(() -> new BadCredentialsException("no refresh"));
        if (!saved.equals(refreshToken)) throw new BadCredentialsException("mismatched refresh");

        // 3) 재발급
        String role = jwt.getRole(saved); // refresh에 role 안 넣었으면 DB/UDS에서 조회
        if (role == null || role.isBlank()) role = "USER";
        String newAt = jwt.createAccessToken(username, role);
        String newRt = jwt.createRefreshToken(username);
        tokenStore.saveRefresh(username, newRt, props.getRefreshExpDays());

        return AuthDtos.TokenRes.builder()
                .accessToken(newAt).refreshToken(newRt)
                .expiresInSec(props.getAccessExpMin()*60).tokenType("Bearer").build();
    }

    public void logout(String accessToken, String username) {
        // access 블랙리스트 남기기 (남은 만료 시간 동안)
        Jws<Claims> j = jwt.parse(accessToken);
        long ttl = (j.getPayload().getExpiration().getTime() - System.currentTimeMillis())/1000;
        if (ttl > 0) tokenStore.blacklist(accessToken, ttl);
        tokenStore.deleteRefresh(username);
    }
}
