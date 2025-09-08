package com.project.board0905.domain.user.controller;

import com.project.board0905.common.error.BusinessException;
import com.project.board0905.common.web.CommonApiResponse;
import com.project.board0905.domain.user.dto.*;
import com.project.board0905.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static com.project.board0905.domain.user.error.UserErrorCode.PASSWORD_CONFIRM_MISMATCH;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "회원 API")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원의 회원가입")
    public ResponseEntity<CommonApiResponse<UserResponse>> signUp(
            @Valid @RequestBody UserSignUpRequest signUpRequest
    ) {
        if (!signUpRequest.getPassword().equals(signUpRequest.getPasswordConfirm())) {
            throw new BusinessException(PASSWORD_CONFIRM_MISMATCH);
        }

        UserResponse created = userService.create(signUpRequest.toCreateRequest());

        // Location 헤더: /api/v1/users/{id}
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/v1/users/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(CommonApiResponse.ok(created));
    }

    @GetMapping("/mypage")
    @Operation(summary = "내 정보 조회")
    public ResponseEntity<CommonApiResponse<UserResponse>> myPage(
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        UserResponse userResponse = userService.myPage(currentUserId);
        return ResponseEntity.ok(CommonApiResponse.ok(userResponse));
    }

    @PatchMapping("/mypage")
    @Operation(summary = "내 정보 수정")
    public ResponseEntity<CommonApiResponse<UserResponse>> updateMe(
            @AuthenticationPrincipal(expression = "userId") Long currentUserId,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        UserResponse res = userService.updateMe(currentUserId, userUpdateRequest);
        return ResponseEntity.ok(CommonApiResponse.ok(res));
    }

    @PatchMapping("/mypage/password")
    @Operation(summary = "비밀번호 변경")
    public ResponseEntity<CommonApiResponse<Void>> changePassword(
            @AuthenticationPrincipal(expression = "userId") Long currentUserId,
            @Valid @RequestBody PasswordChangeRequest req
    ) {
        userService.changePassword(currentUserId, req);
        return ResponseEntity.ok(CommonApiResponse.ok());
    }

    // 생성
    @PostMapping
    @Operation(summary = "수동 회원가입", description = "수동 회원가입")
    public ResponseEntity<CommonApiResponse<UserResponse>> create(
            @Valid @RequestBody UserCreateRequest userCreateRequest
    ) {
        UserResponse res = userService.create(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonApiResponse.ok(res));
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<CommonApiResponse<UserResponse>> get(@PathVariable Long id) {
        UserResponse response = userService.get(id);
        return ResponseEntity.ok(CommonApiResponse.ok(response));
    }

    // 목록 (페이지)
    @GetMapping
    public ResponseEntity<CommonApiResponse<UserPageResponse>> list(Pageable pageable) {
        Page<UserResponse> page = userService.list(pageable);
        return ResponseEntity.ok(
                CommonApiResponse.ok(UserPageResponse.of(page))
        );
    }

    // 수정 (부분 업데이트)
    @PatchMapping("/{id}")
    public ResponseEntity<CommonApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        UserResponse response = userService.update(id, userUpdateRequest);
        return ResponseEntity.ok(CommonApiResponse.ok(response));
    }

    // 삭제 (소프트 삭제: status=DELETED)
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Void>> delete(@PathVariable Long id) {
        userService.softDelete(id);
        return ResponseEntity.ok(CommonApiResponse.ok(null));
    }
}
