package com.project.board0905.domain.admin.service;

import com.project.board0905.domain.admin.dto.AdminBoardUpdateRequest;
import com.project.board0905.domain.admin.dto.AdminOverviewStatsResponse;
import com.project.board0905.domain.admin.dto.AdminRoleUpdateRequest;
import com.project.board0905.domain.admin.dto.AdminUserStatusUpdateRequest;
import com.project.board0905.domain.board.dto.BoardResponse;
import com.project.board0905.domain.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AdminService {
    // 회원 관리
    Page<UserResponse> listUsers(Pageable pageable);
    UserResponse updateUserRole(Long adminId, Long userId, AdminRoleUpdateRequest adminRoleUpdateRequest);
    UserResponse updateUserStatus(Long adminId, Long userId, AdminUserStatusUpdateRequest adminUserStatusUpdateRequest);
    // void resetUserPassword(Long adminId, Long userId, AdminUserPasswordResetRequest req);

    // 게시글 관리
    Page<BoardResponse> listBoards(Pageable pageable);
    BoardResponse updateBoard(Long adminId, Long boardId, AdminBoardUpdateRequest adminBoardUpdateRequest);
    void softDeleteBoard(Long adminId, Long boardId);
    void restoreBoard(Long adminId, Long boardId);
    BoardResponse toggleNotice(Long adminId, Long boardId, boolean notice);


}
