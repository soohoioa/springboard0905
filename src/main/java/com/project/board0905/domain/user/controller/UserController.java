package com.project.board0905.domain.user.controller;

import com.project.board0905.common.web.CommonApiResponse;
import com.project.board0905.domain.user.dto.UserCreateRequest;
import com.project.board0905.domain.user.dto.UserPageResponse;
import com.project.board0905.domain.user.dto.UserResponse;
import com.project.board0905.domain.user.dto.UserUpdateRequest;
import com.project.board0905.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // 생성
    @PostMapping
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
