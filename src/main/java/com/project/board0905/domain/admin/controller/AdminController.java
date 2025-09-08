package com.project.board0905.domain.admin.controller;

import com.project.board0905.common.web.CommonApiResponse;
import com.project.board0905.domain.admin.dto.AdminBoardUpdateRequest;
import com.project.board0905.domain.admin.dto.AdminRoleUpdateRequest;
import com.project.board0905.domain.admin.dto.AdminUserStatusUpdateRequest;
import com.project.board0905.domain.admin.service.AdminService;
import com.project.board0905.domain.board.dto.BoardResponse;
import com.project.board0905.domain.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "관리자 API")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "회원 목록 페이지")
    public ResponseEntity<CommonApiResponse<Page<UserResponse>>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(CommonApiResponse.ok(adminService.listUsers(pageable)));
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "회원 권한 변경")
    public ResponseEntity<CommonApiResponse<UserResponse>> updateUserRole(
            @AuthenticationPrincipal(expression = "userId") Long adminId,
            @PathVariable Long userId,
            @Valid @RequestBody AdminRoleUpdateRequest adminRoleUpdateRequest
    ) {
        return ResponseEntity.ok(CommonApiResponse.ok(adminService.updateUserRole(adminId, userId, adminRoleUpdateRequest)));
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "회원 상태 변경")
    public ResponseEntity<CommonApiResponse<UserResponse>> updateUserStatus(
            @AuthenticationPrincipal(expression = "userId") Long adminId,
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserStatusUpdateRequest adminUserStatusUpdateRequest
    ) {
        return ResponseEntity.ok(CommonApiResponse.ok(adminService.updateUserStatus(adminId, userId, adminUserStatusUpdateRequest)));
    }

    @GetMapping("/boards")
    @Operation(summary = "게시글 목록 페이지")
    public ResponseEntity<CommonApiResponse<Page<BoardResponse>>> listBoards(Pageable pageable) {
        return ResponseEntity.ok(CommonApiResponse.ok(adminService.listBoards(pageable)));
    }

    @PatchMapping("/boards/{boardId}")
    @Operation(summary = "게시글 수정(카테고리/제목/내용/공지/삭제)")
    public ResponseEntity<CommonApiResponse<BoardResponse>> updateBoard(
            @AuthenticationPrincipal(expression = "userId") Long adminId,
            @PathVariable Long boardId,
            @Valid @RequestBody AdminBoardUpdateRequest adminBoardUpdateRequest
    ) {
        return ResponseEntity.ok(CommonApiResponse.ok(adminService.updateBoard(adminId, boardId, adminBoardUpdateRequest)));
    }

    @DeleteMapping("/boards/{boardId}")
    @Operation(summary = "게시글 소프트 삭제")
    public ResponseEntity<CommonApiResponse<Void>> softDeleteBoard(
            @AuthenticationPrincipal(expression = "userId") Long adminId,
            @PathVariable Long boardId
    ) {
        adminService.softDeleteBoard(adminId, boardId);
        return ResponseEntity.ok(CommonApiResponse.ok());
    }

    @PostMapping("/boards/{boardId}/restore")
    @Operation(summary = "게시글 복구")
    public ResponseEntity<CommonApiResponse<Void>> restoreBoard(
            @AuthenticationPrincipal(expression = "userId") Long adminId,
            @PathVariable Long boardId
    ) {
        adminService.restoreBoard(adminId, boardId);
        return ResponseEntity.ok(CommonApiResponse.ok());
    }

    @PostMapping("/boards/{boardId}/notice")
    @Operation(summary = "공지 여부 설정")
    public ResponseEntity<CommonApiResponse<BoardResponse>> toggleNotice(
            @AuthenticationPrincipal(expression = "userId") Long adminId,
            @PathVariable Long boardId,
            @RequestParam boolean notice
    ) {
        return ResponseEntity.ok(CommonApiResponse.ok(adminService.toggleNotice(adminId, boardId, notice)));
    }

}
